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
import ru.mail.jira.plugins.groovy.api.script.CompiledScript;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.binding.LazyDocBindingDescriptorImpl;
import ru.mail.jira.plugins.groovy.api.service.GroovyDocService;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.service.SingletonFactory;
import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;
import ru.mail.jira.plugins.groovy.impl.service.SingletonFactoryImpl;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.cl.ContextAwareClassLoader;
import ru.mail.jira.plugins.groovy.util.cl.DelegatingClassLoader;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component("GlobalObjectsBindingProvider")
@ExportAsDevService(BindingProvider.class)
public class GlobalObjectsBindingProvider implements BindingProvider, PluginLifecycleAware {
    private final Logger logger = LoggerFactory.getLogger(GlobalObjectsBindingProvider.class);

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ThreadLocal<GlobalObjectsState> tempState = new ThreadLocal<>();
    private final ScriptService scriptService;
    private final GlobalObjectDao globalObjectDao;
    private final ExecutionRepository executionRepository;
    private final GroovyDocService groovyDocService;
    private final SingletonFactory singletonFactory;
    private final ContextAwareClassLoader contextAwareClassLoader;
    //we must keep reference to this object
    private final GlobalObjectClassLoader globalObjectClassLoader;
    private volatile GlobalObjectsState state = null;

    @Autowired
    public GlobalObjectsBindingProvider(
            ScriptService scriptService,
            GlobalObjectDao globalObjectDao,
            ExecutionRepository executionRepository,
            DelegatingClassLoader delegatingClassLoader,
            GroovyDocService groovyDocService,
            SingletonFactoryImpl singletonFactory,
            ContextAwareClassLoader contextAwareClassLoader
    ) {
        this.scriptService = scriptService;
        this.globalObjectDao = globalObjectDao;
        this.executionRepository = executionRepository;
        this.groovyDocService = groovyDocService;
        this.singletonFactory = singletonFactory;
        this.contextAwareClassLoader = contextAwareClassLoader;
        this.globalObjectClassLoader = new GlobalObjectClassLoader(this);

        singletonFactory.setGlobalObjectsBindingProvider(this);
        delegatingClassLoader.registerClassLoader("__go", globalObjectClassLoader, false);
        scriptService.registerBindingProvider(this);
    }

    public void refresh() {
        try {
            contextAwareClassLoader.getWriteLock().lockInterruptibly();
            try {
                Lock lock = rwLock.writeLock();
                lock.lock();
                try {
                    unsafeLoadCaches();
                } finally {
                    lock.unlock();
                }
            } finally {
                contextAwareClassLoader.getWriteLock().unlock();
            }
        } catch (InterruptedException e) {
            logger.error("waiting refreshing interrupted", e);
        }
    }

    private void unsafeLoadCaches() {
        try {
            GlobalObjectsState state = new GlobalObjectsState();
            tempState.set(state);

            ClassLoaderUtil.runInContext(() -> {
                logger.info("loading cache");

                state.objects
                        .values()
                        .stream()
                        .map(BindingDescriptor::getType)
                        .forEach(InvokerHelper::removeClass);

                boolean incomplete = false;
                List<GlobalObject> allObjects = new LinkedList<>(globalObjectDao.getAll());
                int prevRemaining = allObjects.size();

                while (true) {
                    allObjectsLoop:
                    for (Iterator<GlobalObject> iterator = allObjects.iterator(); iterator.hasNext(); ) {
                        GlobalObject globalObject = iterator.next();

                        //check if all dependencies are initialized
                        String dependenciesString = globalObject.getDependencies();
                        if (dependenciesString != null) {
                            String[] dependencies = dependenciesString.split(";");

                            for (String dependency : dependencies) {
                                if (!state.types.containsKey(dependency)) {
                                    logger.warn("unable to satisfy dependency on {} for {}", dependency, globalObject.getName());
                                    continue allObjectsLoop;
                                }
                            }
                        }

                        try {
                            long t = System.currentTimeMillis();

                            CompiledScript<?> compiledScript = scriptService.parseSingleton(globalObject.getScriptBody(), true, ImmutableMap.of());
                            Class<?> objectClass = compiledScript.getScriptClass();
                            Object object = singletonFactory.createInstance(compiledScript);

                            state.objects.put(
                                    globalObject.getName(),
                                    new LazyDocBindingDescriptorImpl(object, objectClass, () -> {
                                        try {
                                            return groovyDocService.parseDocs(objectClass.getCanonicalName(), objectClass.getSimpleName(), globalObject.getScriptBody());
                                        } catch (Exception e) {
                                            logger.error("Unable to parse doc for global object {}", globalObject.getName(), e);
                                            return null;
                                        }
                                    })
                            );
                            state.types.put(objectClass.getName(), objectClass);

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

                            iterator.remove();
                        } catch (Exception | NoClassDefFoundError e) {
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

                    //if nothing is initialized in last iteration, consider remaining objects as failed
                    if (prevRemaining == allObjects.size()) {
                        if (allObjects.size() > 0) {
                            logger.error(
                                    "Failed to initialize objects: {}",
                                    allObjects.stream().map(GlobalObject::getName).collect(Collectors.joining(";"))
                            );
                        }
                        break;
                    }

                    prevRemaining = allObjects.size();
                }

                logger.info("loaded global object cache (incomplete: {})", incomplete);
                this.state = state;

                return null;
            });
        } finally {
            tempState.remove();
        }
    }

    @Override
    public Map<String, BindingDescriptor<?>> getBindings() {
        GlobalObjectsState tempState = this.tempState.get();
        if (tempState != null) {
            return tempState.objects;
        }

        if (state == null) {
            throw new IllegalStateException("ContextAwareClassLoader is not initialized");
        }

        Lock rLock = rwLock.readLock();
        rLock.lock();
        try {
            return state.objects;
        } finally {
            rLock.unlock();
        }
    }

    public Map<String, Class> getTypes() {
        GlobalObjectsState tempState = this.tempState.get();
        if (tempState != null) {
            return tempState.types;
        }

        if (state == null) {
            throw new IllegalStateException("ContextAwareClassLoader is not initialized");
        }

        Lock rLock = rwLock.readLock();
        rLock.lock();
        try {
            return state.types;
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public void onStart() {
        if (state == null) {
            refresh();
        }
    }

    @Override
    public void onStop() {
        Lock wLock = rwLock.writeLock();
        wLock.lock();
        try {
            this.state
                    .objects
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

    /**
     * UNSAFE - for tests only
     */
    public void deinitialize() {
        this.state = null;
    }

    private static class GlobalObjectsState {
        private final Map<String, BindingDescriptor<?>> objects = new HashMap<>();
        private final Map<String, Class> types = new HashMap<>();
    }
}
