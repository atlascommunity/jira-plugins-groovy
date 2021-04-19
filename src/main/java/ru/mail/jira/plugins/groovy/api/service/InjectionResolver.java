package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public interface InjectionResolver {
    <T> T resolvePluginInjection(String pluginKey, String className) throws ClassNotFoundException;

    <T> T resolvePluginInjection(Plugin plugin, Class<T> type);

    <T> T resolveStandardInjection(String className) throws ClassNotFoundException;

    <T> T resolveStandardInjection(Class<T> type);

    Plugin getPlugin(String pluginKey);

    default Set<Plugin> getPlugins(Set<String> pluginKeys) {
        Set<Plugin> set = new HashSet<>();
        for (String pluginKey : pluginKeys) {
            Plugin plugin = getPlugin(pluginKey);

            if (plugin == null) {
                throw new RuntimeException("Plugin " + pluginKey + " couldn't be loaded");
            }

            set.add(plugin);
        }
        return set;
    }
}
