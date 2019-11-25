package ru.mail.jira.plugins.groovy.util.cl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class DelegatingClassLoader extends ClassLoader {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Logger logger = LoggerFactory.getLogger(DelegatingClassLoader.class);
    private final Map<String, Reference<ClassLoader>> classLoaders;

    public DelegatingClassLoader(ContextAwareClassLoader contextAwareClassLoader) {
        super(null);
        this.classLoaders = new LinkedHashMap<>();
        this.classLoaders.put("__local", new WeakReference<>(Thread.currentThread().getContextClassLoader()));
        //loader for jira core classes
        this.classLoaders.put("__jira", new WeakReference<>(ClassLoaderUtil.getJiraClassLoader()));
        //loader for WithPlugin classes
        this.classLoaders.put("__context", new WeakReference<>(contextAwareClassLoader));
    }

    public void registerClassLoader(String key, ClassLoader classLoader) {
        Lock wLock = rwLock.writeLock();
        wLock.lock();
        try {
            this.classLoaders.put(key, new WeakReference<>(classLoader));
        } finally {
            wLock.unlock();
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Lock lock = rwLock.readLock();
        lock.lock();
        try {
            for (Map.Entry<String, Reference<ClassLoader>> entry : classLoaders.entrySet()) {
                try {
                    ClassLoader classLoader = entry.getValue().get();

                    if (classLoader == null) {
                        logger.warn("classloader for {} is not present", entry.getKey());
                        continue;
                    }

                    return classLoader.loadClass(name);
                } catch (ClassNotFoundException ignore) {
                }
            }
            throw new ClassNotFoundException(name);
        } finally {
            lock.unlock();
        }
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
        return this.classLoaders.get("__jira").get();
    }
}
