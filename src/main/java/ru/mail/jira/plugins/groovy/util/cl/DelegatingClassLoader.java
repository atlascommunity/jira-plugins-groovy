package ru.mail.jira.plugins.groovy.util.cl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class DelegatingClassLoader extends ClassLoader {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Logger logger = LoggerFactory.getLogger(DelegatingClassLoader.class);
    private final LoadingCache<String, Class<?>> classCache = Caffeine
        .newBuilder()
        .maximumSize(500)
        .expireAfterAccess(20, TimeUnit.MINUTES)
        .recordStats()
        .build(this::doLoadClass);

    private final Map<String, ClassLoaderEntry> classLoaders;

    public DelegatingClassLoader(ContextAwareClassLoader contextAwareClassLoader) {
        super(null);
        this.classLoaders = new LinkedHashMap<>();
        this.classLoaders.put("__local", new ClassLoaderEntry(Thread.currentThread().getContextClassLoader(), true));
        //loader for jira core classes
        this.classLoaders.put("__jira", new ClassLoaderEntry(ClassLoaderUtil.getJiraClassLoader(), true));
        //loader for WithPlugin classes
        this.classLoaders.put("__context", new ClassLoaderEntry(contextAwareClassLoader, false));
    }

    public void registerClassLoader(String key, ClassLoader classLoader, boolean cacheable) {
        this.classLoaders.put(key, new ClassLoaderEntry(classLoader, cacheable));
    }

    public void flushCache() {
        classCache.invalidateAll();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> aClass = classCache.get(name);

        if (aClass == null) {
            aClass = doLoadClass(name, false);
        }

        if (aClass == null) {
            throw new ClassNotFoundException(name);
        }

        return aClass;
    }

    private Class<?> doLoadClass(String name) {
        return doLoadClass(name, true);
    }

    private Class<?> doLoadClass(String name, boolean cacheable) {
        for (Map.Entry<String, ClassLoaderEntry> entry : classLoaders.entrySet()) {
            ClassLoaderEntry e = entry.getValue();
            if (e.cacheable == cacheable) {
                try {
                    ClassLoader classLoader = e.reference;

                    if (classLoader == null) {
                        logger.warn("classloader for {} is gone", entry.getKey());
                        continue;
                    }

                    return classLoader.loadClass(name);
                } catch (ClassNotFoundException ignore) {
                }
            }
        }
        return null;
    }

    @Override
    protected URL findResource(String name) {
        return ClassLoaderUtil.getCurrentPluginClassLoader().getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return ClassLoaderUtil.getCurrentPluginClassLoader().getResources(name);
    }

    public ClassLoader getJiraClassLoader() {
        return this.classLoaders.get("__jira").reference;
    }

    @AllArgsConstructor
    private static class ClassLoaderEntry {
        private final ClassLoader reference;
        private final boolean cacheable;
    }
}
