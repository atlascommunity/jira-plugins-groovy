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
import ru.mail.jira.plugins.groovy.api.script.BindingDescriptor;
import ru.mail.jira.plugins.groovy.api.script.BindingDescriptorImpl;
import ru.mail.jira.plugins.groovy.api.script.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.cl.DelegatingClassLoader;
import ru.mail.jira.plugins.groovy.util.init.PluginLifecycleAware;

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
    private volatile boolean incomplete = false;

    private final ScriptService scriptService;
    private final GlobalObjectDao globalObjectDao;
    private final ExecutionRepository executionRepository;
    private final DelegatingClassLoader delegatingClassLoader;
    //we must keep reference to this object
    private final GlobalObjectClassLoader globalObjectClassLoader;

    @Autowired
    public GlobalObjectsBindingProvider(
        ScriptService scriptService,
        GlobalObjectDao globalObjectDao,
        ExecutionRepository executionRepository,
        DelegatingClassLoader delegatingClassLoader
    ) {
        this.scriptService = scriptService;
        this.globalObjectDao = globalObjectDao;
        this.executionRepository = executionRepository;
        this.delegatingClassLoader = delegatingClassLoader;
        this.globalObjectClassLoader = new GlobalObjectClassLoader(this);
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

            Map<String, BindingDescriptor<?>> objects = new HashMap<>();
            Map<String, Class> types = new HashMap<>();

            boolean incomplete = false;

            objects
                .values()
                .stream()
                .map(BindingDescriptor::getType)
                .forEach(InvokerHelper::removeClass);

            for (GlobalObject globalObject : globalObjectDao.getAll()) {
                try {
                    Class objectClass = scriptService.parseClassStatic(globalObject.getScriptBody(), true, ImmutableMap.of());
                    InvokerHelper.removeClass(objectClass);
                    Object object = objectClass.getConstructor().newInstance();
                    objects.put(
                        globalObject.getName(),
                        new BindingDescriptorImpl(object, object.getClass())
                    );
                    types.put(objectClass.getName(), objectClass);
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
            this.types = types;
            this.incomplete = incomplete;

            logger.info("loaded global object cache (incomplete: {})", incomplete);

            return null;
        });
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

    public Map<String, Class> getTypes() {
        return types;
    }

    @Override
    public void onStart() {
        this.unsafeLoadCaches();
        delegatingClassLoader.registerClassLoader("__go", globalObjectClassLoader);
        scriptService.registerBindingProvider(this);
    }

    @Override
    public void onStop() {}

    @Override
    public int getInitOrder() {
        return 0;
    }
}
