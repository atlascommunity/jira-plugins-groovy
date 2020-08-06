package ru.mail.jira.plugins.groovy.impl.jql;

import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.dom4j.tree.DefaultElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlFunction;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlQueryFunction;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlValuesFunction;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;
import ru.mail.jira.plugins.groovy.api.service.SingletonFactory;
import ru.mail.jira.plugins.groovy.impl.jql.function.QueryFunctionAdapter;
import ru.mail.jira.plugins.groovy.impl.jql.function.ScriptedFunctionAdapter;
import ru.mail.jira.plugins.groovy.impl.jql.function.ValuesFunctionAdapter;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.cl.ContextAwareClassLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Component
public class ModuleManager {
    private final Logger logger = LoggerFactory.getLogger(ModuleReplicationService.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, ServiceRegistration> registeredServices = new ConcurrentHashMap<>();
    private final Map<String, String> moduleKeyToFunction = new ConcurrentHashMap<>();
    private final Map<String, CustomFunction> allFunctions = new ConcurrentHashMap<>();

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PluginAccessor pluginAccessor;
    private final BundleContext bundleContext;
    private final SingletonFactory singletonFactory;
    private final ScriptService scriptService;

    @Autowired
    public ModuleManager(
        @ComponentImport JiraAuthenticationContext jiraAuthenticationContext,
        @ComponentImport PluginAccessor pluginAccessor,
        BundleContext bundleContext,
        ContextAwareClassLoader contextAwareClassLoader,
        SingletonFactory singletonFactory,
        ScriptService scriptService
    ) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.pluginAccessor = pluginAccessor;
        this.bundleContext = bundleContext;
        this.singletonFactory = singletonFactory;
        this.scriptService = scriptService;
    }

    private CustomFunction initializeFunction(JqlFunctionScriptDto script) {
        try {
            Class scriptClass = scriptService.parseSingleton(script.getScriptBody(), false, ImmutableMap.of()).getScriptClass();
            InvokerHelper.removeClass(scriptClass);

            if (ScriptedJqlFunction.class.isAssignableFrom(scriptClass)) {
                Supplier supplier = () -> ClassLoaderUtil.runInContext(
                    () -> singletonFactory.createInstance(
                        scriptService.parseSingleton(script.getScriptBody(), false, ImmutableMap.of())
                    )
                );

                if (ScriptedJqlValuesFunction.class.isAssignableFrom(scriptClass)) {
                    return new ValuesFunctionAdapter(
                        getScriptModuleKey(script.getId()),
                        script.getName(),
                        supplier
                    );
                } else if (ScriptedJqlQueryFunction.class.isAssignableFrom(scriptClass)) {
                    return new QueryFunctionAdapter(
                        getScriptModuleKey(script.getId()),
                        script.getName(),
                        supplier
                    );
                } else {
                    logger.error("Constructed object is not instance of ScriptedJqlFunction {} ({})", script.getName(), script.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Unable to initialize function {} ({})", script.getName(), script.getId(), e);
        }

        return null;
    }

    private JqlFunctionModuleDescriptorImpl getModuleDescriptor(Plugin plugin, CustomFunction function) {
        JqlFunctionModuleDescriptorImpl module = new JqlFunctionModuleDescriptorImpl(
            jiraAuthenticationContext,
            new ModuleFactory() {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException {
                    return (T) function;
                }
            }
        );

        DefaultElement element = new DefaultElement("jql-function");
        element.addAttribute("key", function.getModuleKey());
        DefaultElement fname = new DefaultElement("fname");
        fname.setText(function.getFunctionName());
        element.add(fname);
        DefaultElement list = new DefaultElement("list");
        list.setText(String.valueOf(function.isList()));
        element.add(list);
        module.init(plugin, element);

        return module;
    }

    private void registerDescriptor(String functionName, ModuleDescriptor descriptor) {
        logger.debug("registering function with name: {}", functionName);
        Lock lock = this.lock.writeLock();

        lock.lock();
        try {
            String moduleKey = descriptor.getCompleteKey();
            unregisterDescriptor(moduleKey);

            registeredServices.put(
                descriptor.getKey(),
                bundleContext.registerService(ModuleDescriptor.class.getName(), descriptor, null)
            );
            allFunctions.put(functionName.toLowerCase(), (CustomFunction) descriptor.getModule());
            moduleKeyToFunction.put(descriptor.getKey(), functionName);
        } finally {
            lock.unlock();
        }
    }

    private void unregisterDescriptor(String moduleKey) {
        Lock lock = this.lock.writeLock();

        lock.lock();
        try {
            ServiceRegistration existingRegistration = registeredServices.remove(moduleKey);
            if (existingRegistration != null) {
                try {
                    existingRegistration.unregister();

                    String functionName = moduleKeyToFunction.remove(moduleKey);

                    logger.debug("unregistering function with name: {}", functionName);

                    CustomFunction function = allFunctions.remove(functionName.toLowerCase());
                    if (function instanceof ScriptedFunctionAdapter) {
                        ((ScriptedFunctionAdapter) function).reset();
                    }
                } catch (IllegalStateException e) {
                    logger.debug("already unregistered", e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void registerScript(JqlFunctionScriptDto script) {
        CustomFunction function = initializeFunction(script);
        if (function != null) {
            registerDescriptor(script.getName(), getModuleDescriptor(getCurrentPlugin(), function));
        }
    }

    public void unregisterScript(int id) {
        unregisterDescriptor(getScriptModuleKey(id));
    }

    public void registerBuiltInFunction(CustomFunction function) {
        registerDescriptor(function.getFunctionName(), getModuleDescriptor(getCurrentPlugin(), function));
    }

    public void unregisterAll() {
        Lock lock = this.lock.readLock();

        lock.lock();
        try {
            for (ServiceRegistration serviceRegistration : registeredServices.values()) {
                try {
                    serviceRegistration.unregister();
                    //todo: check if we need to unregister modules when plugin is disabled
                } catch (IllegalStateException e) {
                    logger.debug("already unregistered", e);
                } catch (Exception e) {
                    logger.error("unable to unregister {}", serviceRegistration);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void resetDelegates() {
        Lock lock = this.lock.readLock();

        lock.lock();

        try {
            for (CustomFunction function : allFunctions.values()) {
                if (function instanceof ScriptedFunctionAdapter) {
                    ((ScriptedFunctionAdapter) function).reset();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     *   Returns all managed functions. Keys are all lower case.
     */
    public Map<String, CustomFunction> getAllFunctions() {
        Lock lock = this.lock.readLock();

        lock.lock();
        try {
            return allFunctions;
        } finally {
            lock.unlock();
        }
    }

    private String getScriptModuleKey(int id) {
        return "custom-function-user-" + id;
    }

    private Plugin getCurrentPlugin() {
        return pluginAccessor.getPlugin(Const.PLUGIN_KEY);
    }
}
