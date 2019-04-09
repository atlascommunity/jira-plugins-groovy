package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.plugin.Plugin;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;

import java.util.Map;

public class MockInjectionResolver implements InjectionResolver {
    private final Map<Class, Object> objects;
    private final Map<String, Plugin> plugins;

    public MockInjectionResolver(Map<Class, Object> objects, Map<String, Plugin> plugins) {
        this.objects = objects;
        this.plugins = plugins;
    }

    @Override
    public <T> T resolvePluginInjection(String pluginKey, String className) throws ClassNotFoundException {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public <T> T resolvePluginInjection(Plugin plugin, Class<T> type) {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public <T> T resolveStandardInjection(String className) throws ClassNotFoundException {
        return (T) objects.get(Thread.currentThread().getContextClassLoader().loadClass(className));
    }

    @Override
    public <T> T resolveStandardInjection(Class<T> type) {
        return (T) objects.get(type);
    }

    @Override
    public Plugin getPlugin(String pluginKey) {
        return plugins.get(pluginKey);
    }
}
