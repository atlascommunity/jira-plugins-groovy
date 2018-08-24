package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisablingEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
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
import ru.mail.jira.plugins.groovy.api.script.CompiledScript;
import ru.mail.jira.plugins.groovy.api.dto.CacheStatsDto;
import ru.mail.jira.plugins.groovy.api.service.GlobalFunctionManager;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.impl.groovy.*;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.CompileStaticExtension;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.DeprecatedAstVisitor;
import ru.mail.jira.plugins.groovy.impl.var.GlobalVariable;
import ru.mail.jira.plugins.groovy.impl.var.HttpClientGlobalVariable;
import ru.mail.jira.plugins.groovy.impl.var.LoggerGlobalVariable;
import ru.mail.jira.plugins.groovy.impl.var.TemplateEngineGlobalVariable;
import ru.mail.jira.plugins.groovy.util.DelegatingClassLoader;

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
    private final Map<String, GlobalVariable> globalVariables = new HashMap<>();
    private final Map<String, Class> globalVariableTypes = new HashMap<>();
    private final ParseContextHolder parseContextHolder = new ParseContextHolder();
    private final Cache<String, CompiledScript> scriptCache = Caffeine
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .recordStats()
        .build();

    private final PluginAccessor pluginAccessor;
    private final PluginEventManager pluginEventManager;
    private final GlobalFunctionManager globalFunctionManager;
    private final DelegatingClassLoader classLoader;
    private final GroovyClassLoader gcl;
    private final CompilerConfiguration compilerConfiguration;

    @Autowired
    public ScriptServiceImpl(
        @ComponentImport PluginAccessor pluginAccessor,
        @ComponentImport PluginEventManager pluginEventManager,
        GlobalFunctionManager globalFunctionManager,
        DelegatingClassLoader classLoader
    ) {
        this.pluginAccessor = pluginAccessor;
        this.pluginEventManager = pluginEventManager;
        this.globalFunctionManager = globalFunctionManager;
        this.classLoader = classLoader;
        this.compilerConfiguration = new CompilerConfiguration()
            .addCompilationCustomizers(
                new CompileStaticExtension(parseContextHolder, this),
                new ImportCustomizer().addStarImports("ru.mail.jira.plugins.groovy.api.script"),
                new WithPluginGroovyExtension(parseContextHolder),
                new LoadClassesExtension(parseContextHolder, pluginAccessor, classLoader),
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
    public Map<String, Class> getGlobalVariableTypes() {
        return globalVariableTypes;
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
        //todo: r lock

        logger.debug("started execution");

        CompiledScript compiledScript = null;

        boolean scriptIdPresent = scriptId != null;
        if (scriptIdPresent) {
            compiledScript = scriptCache.getIfPresent(scriptId);
        }

        if (compiledScript == null) {
            compiledScript = parseClass(scriptString, false, compileStatic, types);

            if (scriptIdPresent) {
                scriptCache.put(scriptId, compiledScript);
            }
        }

        //make sure that plugin classes can be loaded for cached scripts
        Set<Plugin> plugins = new HashSet<>();
        for (String pluginKey : compiledScript.getParseContext().getPlugins()) {
            Plugin plugin = pluginAccessor.getPlugin(pluginKey);

            if (plugin == null) {
                throw new RuntimeException("Plugin " + pluginKey + " couldn't be loaded");
            }

            plugins.add(plugin);
        }

        classLoader.ensureAvailability(plugins);

        logger.debug("created class");

        HashMap<String, Object> bindings = new HashMap<>(externalBindings);
        bindings.put("scriptType", type);

        for (ScriptInjection injection : compiledScript.getParseContext().getInjections()) {
            if (injection.getPlugin() != null) {
                Plugin plugin = pluginAccessor.getPlugin(injection.getPlugin());
                Class pluginClass = plugin.getClassLoader().loadClass(injection.getClassName());
                Object component = ComponentAccessor.getOSGiComponentInstanceOfType(pluginClass);

                if (component == null) {
                    List<ModuleDescriptor> modules = plugin.getModuleDescriptorsByModuleClass(pluginClass);
                    if (modules.size() > 0) {
                        component = modules.get(0).getModule();
                    }
                }

                if (component != null) {
                    bindings.put(injection.getVariableName(), component);
                    continue;
                }
            } else {
                Class componentClass = JiraUtils.class.getClassLoader().loadClass(injection.getClassName());
                Object component = ComponentAccessor.getComponent(componentClass);

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

        for (Map.Entry<String, GlobalVariable> entry : globalVariables.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue().getValue());
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

    @PluginEventListener
    public void onPluginUnloaded(PluginDisablingEvent event) {
        String pluginKey = event.getPlugin().getKey();

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
        pluginEventManager.register(this);

        for (Map.Entry<String, String> entry : globalFunctionManager.getGlobalFunctions().entrySet()) {
            globalFunctions.put(entry.getKey(), new ScriptClosure(parseClass(entry.getValue(), false, false, null).getScriptClass()));
        }

        globalVariables.put("httpClient", new HttpClientGlobalVariable());
        globalVariables.put("logger", new LoggerGlobalVariable());
        globalVariables.put("log", new LoggerGlobalVariable());
        globalVariables.put("templateEngine", new TemplateEngineGlobalVariable(gcl));

        globalVariables.forEach((key, var) -> globalVariableTypes.put(key, var.getType()));
        globalVariableTypes.put("scriptType", ScriptType.class);
    }

    @Override
    public void onStop() {
        //clear everything, just in case

        logger.info("cleaning up");

        pluginEventManager.unregister(this);

        invalidateAll();

        globalFunctions.values().forEach(closure -> InvokerHelper.removeClass(closure.getScriptClass()));
        globalFunctions.clear();

        try {
            gcl.close();
        } catch (IOException e) {
            logger.error("unable to close gcl", e);
        }

        for (Map.Entry<String, GlobalVariable> entry : globalVariables.entrySet()) {
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
