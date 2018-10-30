package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;

import java.util.Collection;
import java.util.List;

@Component
public class InjectionResolverImpl implements InjectionResolver {
    private final PluginAccessor pluginAccessor;

    @Autowired
    public InjectionResolverImpl(
        @ComponentImport PluginAccessor pluginAccessor
    ) {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public <T> T resolvePluginInjection(String pluginKey, String className) throws ClassNotFoundException {
        Plugin plugin = getPlugin(pluginKey);

        if (plugin == null) {
            throw new IllegalArgumentException("Invalid plugin " + pluginKey);
        }

        Class pluginClass = plugin.getClassLoader().loadClass(className);

        return (T) resolvePluginInjection(plugin, pluginClass);
    }

    @Override
    public <T> T resolvePluginInjection(Plugin plugin, Class<T> pluginClass) {
        Object component = ComponentAccessor.getOSGiComponentInstanceOfType(pluginClass);

        if (component == null) {
            List<ModuleDescriptor<T>> modules = plugin.getModuleDescriptorsByModuleClass(pluginClass);
            if (modules.size() > 0) {
                component = modules.get(0).getModule();
            }
        }

        if (component == null && plugin instanceof ContainerManagedPlugin) {
            ContainerAccessor containerAccessor = ((ContainerManagedPlugin) plugin).getContainerAccessor();

            Collection beansOfType = containerAccessor.getBeansOfType(pluginClass);
            if (beansOfType.size() > 0) {
                component = beansOfType.iterator().next();
            }
        }

        return (T) component;
    }

    @Override
    public <T> T resolveStandardInjection(String className) throws ClassNotFoundException {
        Class<T> componentClass = (Class<T>) JiraUtils.class.getClassLoader().loadClass(className);
        return resolveStandardInjection(componentClass);
    }

    @Override
    public <T> T resolveStandardInjection(Class<T> type) {
        return ComponentAccessor.getComponent(type);
    }

    @Override
    public Plugin getPlugin(String key) {
        Plugin plugin = pluginAccessor.getPlugin(key);

        return plugin.getPluginState() == PluginState.ENABLED ? plugin : null;
    }
}
