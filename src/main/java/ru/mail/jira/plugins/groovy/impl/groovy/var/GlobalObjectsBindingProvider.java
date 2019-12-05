package ru.mail.jira.plugins.groovy.impl.groovy.var;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dao.GlobalObjectDao;
import ru.mail.jira.plugins.groovy.api.entity.GlobalObject;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.script.binding.LazyDocBindingDescriptorImpl;
import ru.mail.jira.plugins.groovy.api.service.GroovyDocService;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.service.SingletonFactory;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.cl.DelegatingClassLoader;
import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component("GlobalObjectsBindingProvider")
@ExportAsDevService(BindingProvider.class)
public class GlobalObjectsBindingProvider implements BindingProvider, PluginLifecycleAware {
    private final Logger logger = LoggerFactory.getLogger(GlobalObjectsBindingProvider.class);

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile Map<String, BindingDescriptor<?>> objects = ImmutableMap.of();
    private volatile Map<String, Class> types = ImmutableMap.of();
    private volatile boolean initialized = false;

    private final ScriptService scriptService;
    private final GlobalObjectDao globalObjectDao;
    private final ExecutionRepository executionRepository;
    private final GroovyDocService groovyDocService;
    private final SingletonFactory singletonFactory;
    //we must keep reference to this object
    private final GlobalObjectClassLoader globalObjectClassLoader;

    @Autowired
    public GlobalObjectsBindingProvider(
        ScriptService scriptService,
        GlobalObjectDao globalObjectDao,
        ExecutionRepository executionRepository,
        DelegatingClassLoader delegatingClassLoader,
        GroovyDocService groovyDocService,
        SingletonFactory singletonFactory
    ) {
        this.scriptService = scriptService;
        this.globalObjectDao = globalObjectDao;
        this.executionRepository = executionRepository;
        this.groovyDocService = groovyDocService;
        this.singletonFactory = singletonFactory;
        this.globalObjectClassLoader = new GlobalObjectClassLoader(this);

        delegatingClassLoader.registerClassLoader("__go", globalObjectClassLoader, false);
        scriptService.registerBindingProvider(this);
    }

    public void refresh() {
        Lock lock = rwLock.writeLock();

        lock.lock();
        try {
            unsafeLoadCaches();
        } finally {
            lock.unlock();
        }
    }

    private void unsafeLoadCaches() {
        ClassLoaderUtil.runInContext(() -> {
            logger.info("loading cache");

            objects
                .values()
                .stream()
                .map(BindingDescriptor::getType)
                .forEach(InvokerHelper::removeClass);

            Map<String, BindingDescriptor<?>> objects = new HashMap<>();
            Map<String, Class> types = new HashMap<>();

            boolean incomplete = false;

            for (GlobalObject globalObject : globalObjectDao.getAll()) {
                try {
                    long t = System.currentTimeMillis();

                    Class objectClass = scriptService.parseClassStatic(globalObject.getScriptBody(), true, ImmutableMap.of());
                    Object object = singletonFactory.createInstance(objectClass);
                    objects.put(
                        globalObject.getName(),
                        new LazyDocBindingDescriptorImpl(object, object.getClass(), () -> {
                            try {
                                return groovyDocService.parseDocs(objectClass.getCanonicalName(), objectClass.getSimpleName(), globalObject.getScriptBody());
                            } catch (Exception e) {
                                logger.error("Unable to parse doc for global object {}", globalObject.getName(), e);
                                return null;
                            }
                        })
                    );
                    types.put(objectClass.getName(), objectClass);

                    t = System.currentTimeMillis() - t;

                    if (t >= ExecutionRepository.WARNING_THRESHOLD) {
                        executionRepository.trackInline(
                            globalObject.getUuid(),
                            t,
                            true,
                            null,
                            ImmutableMap.of(
                                "type", ScriptType.GLOBAL_OBJECT.name()
                            )
                        );
                    }
                } catch (Exception e) {
                    logger.error("Unable to initialize global object {}", globalObject.getName(), e);
                    executionRepository.trackInline(
                        globalObject.getUuid(),
                        0L,
                        false,
                        e,
                        ImmutableMap.of(
                            "type", ScriptType.GLOBAL_OBJECT.name()
                        )
                    );
                }
            }

            this.objects = objects;
            this.types = types;

            logger.info("loaded global object cache (incomplete: {})", incomplete);

            return null;
        });
    }

    private void unsafeInitialize() {
        this.unsafeLoadCaches();
        initialized = true;
    }

    private void initializePrematurely() {
        Lock wLock = rwLock.writeLock();
        wLock.lock();
        try {
            if (initialized) {
                return;
            }

            logger.warn("doing premature initialization");
            unsafeInitialize();
        } finally {
            wLock.unlock();
        }
    }

    @Override
    public Map<String, BindingDescriptor<?>> getBindings() {
        if (!initialized) {
            initializePrematurely();
        }

        Lock rLock = rwLock.readLock();
        rLock.lock();
        try {
            return this.objects;
        } finally {
            rLock.unlock();
        }
    }

    public Map<String, Class> getTypes() {
        return types;
    }

    @Override
    public void onStart() {
        if (!initialized) {
            ReentrantReadWriteLock.WriteLock wLock = rwLock.writeLock();
            wLock.lock();
            try {
                if (!initialized) {
                    unsafeInitialize();
                }
            } finally {
                wLock.unlock();
            }
        }
    }

    @Override
    public void onStop() {
        Lock wLock = rwLock.writeLock();
        wLock.lock();
        try {
            this.objects
                .values()
                .stream()
                .map(BindingDescriptor::getType)
                .forEach(InvokerHelper::removeClass);
        } finally {
            wLock.unlock();
        }
    }

    @Override
    public int getInitOrder() {
        return 11;
    }
}
