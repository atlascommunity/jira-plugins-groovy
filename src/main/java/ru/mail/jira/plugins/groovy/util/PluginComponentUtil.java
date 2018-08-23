package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;

import java.util.List;

public class PluginComponentUtil {
    public static <T> T getPluginComponent(Plugin plugin, Class<T> componentClass) {
        T component = ComponentAccessor.getOSGiComponentInstanceOfType(componentClass);

        if (component == null) {
            List<ModuleDescriptor<T>> modules = plugin.getModuleDescriptorsByModuleClass(componentClass);
            if (modules.size() > 0) {
                component = modules.get(0).getModule();
            }
        }

        return component;
    }
}
