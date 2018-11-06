package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.collect.ImmutableMap;
import groovy.lang.*;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.tools.GroovyClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.script.*;
import ru.mail.jira.plugins.groovy.api.dto.CacheStatsDto;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingProvider;
import ru.mail.jira.plugins.groovy.api.service.GlobalFunctionManager;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.groovy.*;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.CompileStaticExtension;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.DeprecatedAstVisitor;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;
import ru.mail.jira.plugins.groovy.impl.var.HttpClientBindingDescriptor;
import ru.mail.jira.plugins.groovy.impl.var.LoggerBindingDescriptor;
import ru.mail.jira.plugins.groovy.impl.var.TemplateEngineBindingDescriptor;
import ru.mail.jira.plugins.groovy.util.cl.DelegatingClassLoader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ExportAsService({ScriptService.class, LifecycleAware.class})
@Component
public class ScriptServiceImpl implements ScriptService, LifecycleAware {
    private final Logger logger = LoggerFactory.getLogger(ScriptServiceImpl.class);
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(); //todo: remove?
    private final Map<String, ScriptClosure> globalFunctions = new HashMap<>();
    private final Map<String, BindingDescriptor> globalVariables = new HashMap<>();
    private final Map<String, Class> globalVariableTypes = new HashMap<>();
    private final ParseContextHolder parseContextHolder = new ParseContextHolder();
    //assuming that all mutations happen only during initialization in single thread
    private final List<BindingProvider> bindingProviders = new ArrayList<>();
    private final Cache<String, CompiledScript> scriptCache = Caffeine
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .recordStats()
        .build();

    private final InjectionResolver injectionResolver;
    private final GlobalFunctionManager globalFunctionManager;
    private final DelegatingClassLoader classLoader;
    private final GroovyClassLoader gcl;
    private final CompilerConfiguration compilerConfiguration;

    @Autowired
    public ScriptServiceImpl(
        InjectionResolver injectionResolver,
        GlobalFunctionManager globalFunctionManager,
        DelegatingClassLoader classLoader
    ) {
        this.injectionResolver = injectionResolver;
        this.globalFunctionManager = globalFunctionManager;
        this.classLoader = classLoader;
        this.compilerConfiguration = new CompilerConfiguration()
            .addCompilationCustomizers(
                new CompileStaticExtension(parseContextHolder, this),
                new ImportCustomizer().addStarImports("ru.mail.jira.plugins.groovy.api.script"),
                new WithPluginGroovyExtension(parseContextHolder),
                new LoadClassesExtension(parseContextHolder, injectionResolver, classLoader),
                new InjectionExtension(parseContextHolder),
                new ParamExtension(parseContextHolder)
            );
        this.compilerConfiguration.setWarningLevel(WarningMessage.LIKELY_ERRORS);
        this.compilerConfiguration.setTolerance(10);
        this.compilerConfiguration.setOptimizationOptions(ImmutableMap.of(
            CompilerConfiguration.INVOKEDYNAMIC, Boolean.TRUE
        ));
        this.gcl = new GroovyClassLoader(
            classLoader,
            this.compilerConfiguration
        );
    }

    @Override
    public Object executeScript(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings) throws Exception {
        return doExecuteScript(scriptId, scriptString, type, bindings, false, null);
    }

    @Override
    public Object executeScriptStatic(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings, Map<String, Class> types) throws Exception {
        return doExecuteScript(scriptId, scriptString, type, bindings, true, types);
    }

    @Override
    public ParseContext parseScript(String script) {
        CompiledScript result = parseClass(script, true, false, null);
        InvokerHelper.removeClass(result.getScriptClass());
        return result.getParseContext();
    }

    @Override
    public ParseContext parseScriptStatic(String script, Map<String, Class> types) {
        CompiledScript result = parseClass(script, true, true, types);
        InvokerHelper.removeClass(result.getScriptClass());
        return result.getParseContext();
    }

    @Override
    public Class parseClass(String classBody, boolean extended) {
        return parseClass(classBody, extended, false, null).getScriptClass();
    }

    @Override
    public Class parseClassStatic(String classBody, boolean extended, Map<String, Class> types) {
        return parseClass(classBody, extended, true, types).getScriptClass();
    }

    @Override
    public CacheStatsDto getCacheStats() {
        CacheStats stats = scriptCache.stats();

        return new CacheStatsDto(
            stats.hitCount(),
            stats.missCount(),
            stats.loadSuccessCount(),
            stats.loadFailureCount(),
            stats.totalLoadTime(),
            stats.evictionCount(),
            stats.evictionWeight(),
            scriptCache.estimatedSize()
        );
    }

    @Override
    public Map<String, Class> getGlobalBindingTypes() {
        Map<String, Class> result = new HashMap<>(globalVariableTypes);

        for (BindingProvider bindingProvider : bindingProviders) {
            bindingProvider
                .getBindings()
                .forEach((k, v) -> result.put(k, v.getType()));
        }

        return result;
    }

    @Override
    public Map<String, BindingDescriptor> getGlobalBindings() {
        Map<String, BindingDescriptor> result = new HashMap<>(globalVariables);

        for (BindingProvider bindingProvider : bindingProviders) {
            bindingProvider
                .getBindings()
                .forEach(result::put);
        }

        return result;
    }

    @Override
    public void registerBindingProvider(BindingProvider bindingProvider) {
        bindingProviders.add(bindingProvider);
    }

    @Override
    public void invalidate(String id) {
        scriptCache.invalidate(id);
    }

    @Override
    public void invalidateAll() {
        scriptCache.invalidateAll();
        gcl.clearCache();
    }

    private Object doExecuteScript(
        String scriptId, String scriptString, ScriptType type, Map<String, Object> externalBindings,
        boolean compileStatic, Map<String, Class> types
    ) throws Exception {
        logger.debug("started execution");

        CompiledScript compiledScript = null;

        if (scriptId != null) {
            compiledScript = scriptCache.get(scriptId, ignore -> parseClass(scriptString, false, compileStatic, types));
        }

        if (compiledScript == null) {
            compiledScript = parseClass(scriptString, false, compileStatic, types);
        }

        //make sure that plugin classes can be loaded for cached scripts
        Set<Plugin> plugins = new HashSet<>();
        for (String pluginKey : compiledScript.getParseContext().getPlugins()) {
            Plugin plugin = injectionResolver.getPlugin(pluginKey);

            if (plugin == null) {
                throw new RuntimeException("Plugin " + pluginKey + " couldn't be loaded");
            }

            plugins.add(plugin);
        }

        classLoader.ensureAvailability(plugins);

        logger.debug("created class");

        HashMap<String, Object> bindings = new HashMap<>(externalBindings);
        bindings.put("scriptType", type);

        for (BindingProvider bindingProvider : bindingProviders) {
            bindingProvider.getBindings().forEach((name, value) -> bindings.put(name, value.getValue(scriptId)));
        }

        for (ScriptInjection injection : compiledScript.getParseContext().getInjections()) {
            if (injection.getPlugin() != null) {
                Object component = injectionResolver.resolvePluginInjection(injection.getPlugin(), injection.getClassName());

                if (component != null) {
                    bindings.put(injection.getVariableName(), component);
                    continue;
                }
            } else {
                Object component = injectionResolver.resolveStandardInjection(injection.getClassName());

                if (component != null) {
                    bindings.put(injection.getVariableName(), component);
                    continue;
                }
            }

            throw new RuntimeException("Unable to resolve injection: " + injection);
        }

        for (Map.Entry<String, ScriptClosure> entry : globalFunctions.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, BindingDescriptor> entry : globalVariables.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue().getValue(scriptId));
        }

        logger.debug("initialized bindings");

        Script script = InvokerHelper.createScript(compiledScript.getScriptClass(), new Binding(bindings));

        logger.debug("created script");

        try {
            Object result = script.run();
            logger.debug("completed script");

            return result;
        } finally {
            InvokerHelper.removeClass(script.getClass());
        }
    }

    public void onPluginDisable(Plugin plugin) {
        String pluginKey = plugin.getKey();

        Lock lock = rwLock.writeLock();
        lock.lock();
        try {
            classLoader.unloadPlugin(pluginKey);
            scriptCache.invalidateAll();
        } finally {
            lock.unlock();
        }
    }

    private CompiledScript parseClass(String script, boolean extended, boolean compileStatic, Map<String, Class> types) {
        logger.debug("parsing script");
        try {
            parseContextHolder.get().setExtended(extended);
            parseContextHolder.get().setCompileStatic(compileStatic);
            parseContextHolder.get().setTypes(types);

            Class scriptClass = null;
            if (compileStatic) {
                String fileName = "script" + System.currentTimeMillis() + Math.abs(script.hashCode()) + ".groovy";
                CompilationUnit compilationUnit = new CompilationUnit(compilerConfiguration, null, gcl);
                SourceUnit sourceUnit = compilationUnit.addSource(fileName, script);
                compilationUnit.compile(Phases.CLASS_GENERATION);

                if (extended) {
                    DeprecatedAstVisitor astVisitor = new DeprecatedAstVisitor();

                    for (Object aClass : compilationUnit.getAST().getClasses()) {
                        astVisitor.visitClass((ClassNode) aClass);
                    }

                    parseContextHolder.get().setWarnings(astVisitor.getWarnings());
                }

                GroovyClassLoader.InnerLoader innerLoader = new GroovyClassLoader.InnerLoader(gcl);

                String mainClass = sourceUnit.getAST().getMainClassName();
                for (Object aClass : compilationUnit.getClasses()) {
                    if (aClass instanceof GroovyClass) {
                        GroovyClass groovyClass = (GroovyClass) aClass;

                        BytecodeProcessor bytecodePostprocessor = compilerConfiguration.getBytecodePostprocessor();

                        byte[] byteCode = groovyClass.getBytes();
                        if (bytecodePostprocessor != null) {
                            byteCode = bytecodePostprocessor.processBytecode(groovyClass.getName(), byteCode);
                        }

                        Class clazz = innerLoader.defineClass(groovyClass.getName(), byteCode);

                        if (groovyClass.getName().equals(mainClass)) {
                            scriptClass = clazz;
                        }
                    }
                }

                //todo: we might have an issue if some script (parsed by gcl) created class with name Xxx and there's global object with same class name
            } else {
                scriptClass = gcl.parseClass(script);
            }

            logger.debug("parsed script");
            return new CompiledScript(
                scriptClass,
                parseContextHolder.get()
            );
        } finally {
            logger.debug("resetting parse context");
            this.parseContextHolder.reset();
        }
    }

    @Override
    public void onStart() {
        for (Map.Entry<String, String> entry : globalFunctionManager.getGlobalFunctions().entrySet()) {
            globalFunctions.put(entry.getKey(), new ScriptClosure(parseClass(entry.getValue(), false, false, null).getScriptClass()));
        }

        globalVariables.put("httpClient", new HttpClientBindingDescriptor());
        globalVariables.put("logger", new LoggerBindingDescriptor());
        globalVariables.put("log", new LoggerBindingDescriptor());
        globalVariables.put("templateEngine", new TemplateEngineBindingDescriptor(gcl));

        globalVariables.forEach((key, var) -> globalVariableTypes.put(key, var.getType()));
        globalVariableTypes.put("scriptType", ScriptType.class);
    }

    @Override
    public void onStop() {
        //clear everything, just in case

        logger.info("cleaning up");

        invalidateAll();

        globalFunctions.values().forEach(closure -> InvokerHelper.removeClass(closure.getScriptClass()));
        globalFunctions.clear();

        try {
            gcl.close();
        } catch (IOException e) {
            logger.error("unable to close gcl", e);
        }

        for (Map.Entry<String, BindingDescriptor> entry : globalVariables.entrySet()) {
            try {
                entry.getValue().dispose();
            } catch (Exception e) {
                logger.error("unable to dispose variable {}", entry.getKey(), e);
            }
        }

        globalVariables.clear();
        globalVariableTypes.clear();
    }
}
