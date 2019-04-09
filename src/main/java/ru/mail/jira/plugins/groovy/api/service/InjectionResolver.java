package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.plugin.Plugin;

public interface InjectionResolver {
    <T> T resolvePluginInjection(String pluginKey, String className) throws ClassNotFoundException;

    <T> T resolvePluginInjection(Plugin plugin, Class<T> type);

    <T> T resolveStandardInjection(String className) throws ClassNotFoundException;

    <T> T resolveStandardInjection(Class<T> type);

    Plugin getPlugin(String pluginKey);
}
