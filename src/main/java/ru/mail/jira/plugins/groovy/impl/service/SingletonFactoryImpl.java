package ru.mail.jira.plugins.groovy.impl.service;

import com.atlassian.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.script.CompiledScript;
import ru.mail.jira.plugins.groovy.api.script.PluginModule;
import ru.mail.jira.plugins.groovy.api.script.StandardModule;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;
import ru.mail.jira.plugins.groovy.api.service.SingletonFactory;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.cl.ContextAwareClassLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;

@Component
public class SingletonFactoryImpl implements SingletonFactory {
    private final InjectionResolver injectionResolver;
    private final ContextAwareClassLoader contextAwareClassLoader;

    @Autowired
    public SingletonFactoryImpl(
        InjectionResolver injectionResolver,
        ContextAwareClassLoader contextAwareClassLoader
    ) {
        this.injectionResolver = injectionResolver;
        this.contextAwareClassLoader = contextAwareClassLoader;
    }

    private <T> Constructor<T> findSingleConstructor(Class<T> type) {
        Constructor[] constructors = type.getConstructors();

        if (constructors.length == 0) {
            throw new IllegalArgumentException("No public constructors found");
        }

        if (constructors.length > 1) {
            throw new IllegalArgumentException("Found more than one public constructor");
        }

        return constructors[0];
    }

    public <T> Object[] doGetConstructorArguments(CompiledScript<T> compiledScript) {
        Constructor<T> constructor = findSingleConstructor(compiledScript.getScriptClass());

        if (constructor.getParameterCount() == 0) {
            return new Object[0];
        }

        Annotation[][] allParameterAnnotations = constructor.getParameterAnnotations();
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] result = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; ++i) {
            Annotation[] parameterAnnotations = allParameterAnnotations[i];
            Class<?> parameterType = parameterTypes[i];

            if (Arrays.stream(parameterAnnotations).anyMatch(it -> it.annotationType().equals(PluginModule.class))) {
                String bundleName = ClassLoaderUtil.getClassBundleName(parameterType);
                Plugin plugin = injectionResolver.getPlugin(bundleName);

                result[i] = injectionResolver.resolvePluginInjection(plugin, parameterType);
            } else if (Arrays.stream(parameterAnnotations).anyMatch(it -> it.annotationType().equals(StandardModule.class))) {
                result[i] = injectionResolver.resolveStandardInjection(parameterType);
            } else {
                throw new IllegalArgumentException("Parameter at index " + i + " is not annotated");
            }

            if (result[i] == null) {
                throw new IllegalArgumentException("Unable to resolve parameter at index " + i);
            }
        }

        return result;
    }

    @Override
    public <T> Object[] getConstructorArguments(CompiledScript<T> compiledScript) {
        try {
            contextAwareClassLoader.addPlugins(injectionResolver.getPlugins(compiledScript.getParseContext().getPlugins()));
            return doGetConstructorArguments(compiledScript);
        } finally {
            contextAwareClassLoader.clearContext();
        }
    }

    @Override
    public <T> T createInstance(CompiledScript<T> compiledScript) {
        try {
            contextAwareClassLoader.addPlugins(injectionResolver.getPlugins(compiledScript.getParseContext().getPlugins()));
            return (T) findSingleConstructor(compiledScript.getScriptClass()).newInstance(doGetConstructorArguments(compiledScript));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            contextAwareClassLoader.clearContext();
        }
    }
}
