package ru.mail.jira.plugins.groovy.util.cl;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
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
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class DelegatingClassLoader extends ClassLoader {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Logger logger = LoggerFactory.getLogger(DelegatingClassLoader.class);
    //todo: maybe change to Map<String, String(plugin key)> with LoadingCache<String, ClassLoader>
    private final Map<String, Reference<ClassLoader>> classLoaders;

    public DelegatingClassLoader() {
        super(null);
        this.classLoaders = new LinkedHashMap<>();
        this.classLoaders.put("__local", new WeakReference<>(Thread.currentThread().getContextClassLoader()));
        //loader for jira core classes
        this.classLoaders.put("__jira", new WeakReference<>(ClassLoaderUtil.getJiraClassLoader()));
    }

    public void ensureAvailability(Set<Plugin> plugins) {
        if (plugins.size() == 0) {
            return;
        }

        Lock lock = rwLock.writeLock();
        lock.lock();
        try {
            for (Plugin plugin : plugins) {
                if (plugin.getPluginState() != PluginState.ENABLED) {
                    throw new RuntimeException("Plugin " + plugin.getKey() + " is not enabled");
                }
                classLoaders.put(plugin.getKey(), new WeakReference<>(plugin.getClassLoader()));
            }
        } finally {
            lock.unlock();
        }
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

    public void unloadPlugin(String key) {
        Lock wLock = rwLock.writeLock();
        wLock.lock();
        try {
            classLoaders.remove(key);
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
