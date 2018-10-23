package ru.mail.jira.plugins.groovy.impl.groovy.var;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dao.GlobalObjectDao;
import ru.mail.jira.plugins.groovy.api.entity.GlobalObject;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.script.BindingDescriptor;
import ru.mail.jira.plugins.groovy.api.script.BindingDescriptorImpl;
import ru.mail.jira.plugins.groovy.api.script.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.init.PluginLifecycleAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class GlobalObjectsBindingProvider implements BindingProvider, PluginLifecycleAware {
    private final Logger logger = LoggerFactory.getLogger(GlobalObjectsBindingProvider.class);

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile Map<String, BindingDescriptor<?>> objects;
    private volatile boolean incomplete;

    private final ScriptService scriptService;
    private final GlobalObjectDao globalObjectDao;
    private final ExecutionRepository executionRepository;

    @Autowired
    public GlobalObjectsBindingProvider(
        ScriptService scriptService,
        GlobalObjectDao globalObjectDao,
        ExecutionRepository executionRepository
    ) {
        this.scriptService = scriptService;
        this.globalObjectDao = globalObjectDao;
        this.executionRepository = executionRepository;
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
        logger.info("loading cache");

        ConcurrentHashMap<String, BindingDescriptor<?>> objects = new ConcurrentHashMap<>();
        boolean incomplete = false;

        for (GlobalObject globalObject : globalObjectDao.getAll()) {
            try {
                Class<Object> objectClass = scriptService.parseClassStatic(globalObject.getScriptBody(), true, ImmutableMap.of());
                objects.put(
                    globalObject.getName(),
                    new BindingDescriptorImpl<>(
                        objectClass.getConstructor().newInstance(),
                        objectClass
                    )
                );
            } catch (Exception e) {
                logger.error("Unable to initialize global object {}", globalObject.getName(), e);
                executionRepository.trackInline(
                    globalObject.getUuid(),
                    0L,
                    false,
                    ExceptionHelper.writeExceptionToString(e),
                    ImmutableMap.of(
                        "type", ScriptType.GLOBAL_OBJECT.name()
                    ));
                incomplete = true;
            }
        }

        this.objects = objects;
        this.incomplete = incomplete;

        logger.info("loaded global object cache (incomplete: {})", incomplete);
    }

    @Override
    public Map<String, BindingDescriptor<?>> getBindings() {
        if (incomplete) {
            Lock wLock = rwLock.writeLock();
            wLock.lock();
            try {
                if (incomplete) {
                    unsafeLoadCaches();
                }
                return this.objects;
            } finally {
                wLock.unlock();
            }
        }

        Lock rLock = rwLock.readLock();
        rLock.lock();
        try {
            return this.objects;
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public void onStart() {
        this.unsafeLoadCaches();
        scriptService.registerBindingProvider(this);
    }

    @Override
    public void onStop() {}

    @Override
    public int getInitOrder() {
        return 0;
    }
}
