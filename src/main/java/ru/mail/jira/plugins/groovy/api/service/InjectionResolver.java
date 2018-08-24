package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.plugin.Plugin;

public interface InjectionResolver {
    <T> T resolvePluginInjection(String pluginKey, String className) throws ClassNotFoundException;

    <T> T resolveStandardInjection(String className) throws ClassNotFoundException;

    Plugin getPlugin(String pluginKey);
}
