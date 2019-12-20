package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.plugin.Plugin;

import java.util.Set;
import java.util.stream.Collectors;

public interface InjectionResolver {
    <T> T resolvePluginInjection(String pluginKey, String className) throws ClassNotFoundException;

    <T> T resolvePluginInjection(Plugin plugin, Class<T> type);

    <T> T resolveStandardInjection(String className) throws ClassNotFoundException;

    <T> T resolveStandardInjection(Class<T> type);

    Plugin getPlugin(String pluginKey);

    default Set<Plugin> getPlugins(Set<String> pluginKeys) {
        return pluginKeys.stream().map(this::getPlugin).collect(Collectors.toSet());
    }
}
