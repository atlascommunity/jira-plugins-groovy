package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.plugin.Plugin;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;

import java.util.Map;

public class MockInjectionResolver implements InjectionResolver {
    private final Map<Class, Object> objects;

    public MockInjectionResolver(Map<Class, Object> objects) {
        this.objects = objects;
    }

    @Override
    public <T> T resolvePluginInjection(String pluginKey, String className) throws ClassNotFoundException {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public <T> T resolveStandardInjection(String className) throws ClassNotFoundException {
        return (T) objects.get(Thread.currentThread().getContextClassLoader().loadClass(className));
    }

    @Override
    public Plugin getPlugin(String pluginKey) {
        throw new UnsupportedOperationException(); //todo
    }
}
