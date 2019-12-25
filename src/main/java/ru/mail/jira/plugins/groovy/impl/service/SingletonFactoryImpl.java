package ru.mail.jira.plugins.groovy.impl.service;

import com.atlassian.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.script.*;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;
import ru.mail.jira.plugins.groovy.api.service.SingletonFactory;
import ru.mail.jira.plugins.groovy.impl.groovy.var.GlobalObjectsBindingProvider;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.cl.ContextAwareClassLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

@Component
public class SingletonFactoryImpl implements SingletonFactory {
    private final InjectionResolver injectionResolver;
    private final ContextAwareClassLoader contextAwareClassLoader;
    private GlobalObjectsBindingProvider globalObjectsBindingProvider;

    @Autowired
    public SingletonFactoryImpl(
        InjectionResolver injectionResolver,
        ContextAwareClassLoader contextAwareClassLoader
    ) {
        this.injectionResolver = injectionResolver;
        this.contextAwareClassLoader = contextAwareClassLoader;
    }

    private <T> Constructor<T> findSingleConstructor(Class<T> type) {
        Constructor<?>[] constructors = type.getConstructors();

        if (constructors.length == 0) {
            throw new IllegalArgumentException("No public constructors found");
        }

        if (constructors.length > 1) {
            throw new IllegalArgumentException("Found more than one public constructor");
        }

        return (Constructor<T>) constructors[0];
    }

    public <T> ResolvedConstructorArgument[] doGetConstructorArguments(CompiledScript<T> compiledScript) {
        Constructor<T> constructor = findSingleConstructor(compiledScript.getScriptClass());

        if (constructor.getParameterCount() == 0) {
            return new ResolvedConstructorArgument[0];
        }

        Annotation[][] allParameterAnnotations = constructor.getParameterAnnotations();
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        ResolvedConstructorArgument[] result = new ResolvedConstructorArgument[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; ++i) {
            Annotation[] parameterAnnotations = allParameterAnnotations[i];
            Class<?> parameterType = parameterTypes[i];

            if (Arrays.stream(parameterAnnotations).anyMatch(it -> it.annotationType().equals(PluginModule.class))) {
                String bundleName = ClassLoaderUtil.getClassBundleName(parameterType);
                Plugin plugin = injectionResolver.getPlugin(bundleName);

                result[i] = new ResolvedConstructorArgument(
                    ResolvedConstructorArgument.ArgumentType.PLUGIN,
                    injectionResolver.resolvePluginInjection(plugin, parameterType)
                );
            } else if (Arrays.stream(parameterAnnotations).anyMatch(it -> it.annotationType().equals(StandardModule.class))) {
                result[i] = new ResolvedConstructorArgument(
                    ResolvedConstructorArgument.ArgumentType.STANDARD,
                    injectionResolver.resolveStandardInjection(parameterType)
                );
            } else {
                Optional<GlobalObjectModule> globalObjectInjection = Arrays
                    .stream(parameterAnnotations)
                    .filter(it -> it instanceof GlobalObjectModule)
                    .map(it -> (GlobalObjectModule) it)
                    .findAny();

                if (globalObjectInjection.isPresent()) {
                    Optional<BindingDescriptor<?>> bindingDescriptor = globalObjectsBindingProvider
                        .getBindings()
                        .values()
                        .stream()
                        .filter(it -> it.getClass() == parameterType)
                        .findAny();
                    if (bindingDescriptor.isPresent()) {
                        result[i] = new ResolvedConstructorArgument(
                            ResolvedConstructorArgument.ArgumentType.GLOBAL_OBJECT,
                            bindingDescriptor.get().getValue(null, null)
                        );
                    }
                } else {
                    throw new IllegalArgumentException("Parameter at index " + i + " is not annotated");
                }
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
            return getObjects(doGetConstructorArguments(compiledScript));
        } finally {
            contextAwareClassLoader.clearContext();
        }
    }

    @Override
    public <T> ResolvedConstructorArgument[] getExtendedConstructorArguments(CompiledScript<T> compiledScript) throws IllegalArgumentException {
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
            return findSingleConstructor(compiledScript.getScriptClass()).newInstance(getObjects(doGetConstructorArguments(compiledScript)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            contextAwareClassLoader.clearContext();
        }
    }

    public void setGlobalObjectsBindingProvider(GlobalObjectsBindingProvider globalObjectsBindingProvider) {
        this.globalObjectsBindingProvider = globalObjectsBindingProvider;
    }

    private Object[] getObjects(ResolvedConstructorArgument[] arguments) {
        return Arrays
            .stream(arguments)
            .map(ResolvedConstructorArgument::getObject)
            .toArray();
    }
}
