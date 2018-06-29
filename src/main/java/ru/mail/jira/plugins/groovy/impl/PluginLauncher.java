package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.util.concurrent.LazyReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.JqlFunctionServiceImpl;
import ru.mail.jira.plugins.groovy.impl.jql.JqlInitializer;
import ru.mail.jira.plugins.groovy.impl.listener.EventListenerInvoker;
import ru.mail.jira.plugins.groovy.impl.scheduled.ScheduledTaskServiceImpl;
import ru.mail.jira.plugins.groovy.util.Const;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


//we use approach like in com.atlassian.greenhopper.Launcher
@Component
@ExportAsService(LifecycleAware.class)
public final class PluginLauncher implements LifecycleAware {
    private final Logger logger = LoggerFactory.getLogger(PluginLauncher.class);
    private final EventPublisher eventPublisher;
    private final EventListenerInvoker eventListenerInvoker;
    private final ScheduledTaskServiceImpl scheduledTaskService;
    private final OldExecutionDeletionScheduler executionDeletionScheduler;
    private final JqlInitializer jqlInitializer;
    private final JqlFunctionServiceImpl jqlFunctionServiceImpl;

    @Autowired
    public PluginLauncher(
        @ComponentImport EventPublisher eventPublisher,
        EventListenerInvoker eventListenerInvoker,
        ScheduledTaskServiceImpl scheduledTaskService,
        OldExecutionDeletionScheduler executionDeletionScheduler,
        JqlInitializer jqlInitializer,
        JqlFunctionServiceImpl jqlFunctionServiceImpl
    ) {
        this.eventPublisher = eventPublisher;
        this.eventListenerInvoker = eventListenerInvoker;
        this.scheduledTaskService = scheduledTaskService;
        this.executionDeletionScheduler = executionDeletionScheduler;
        this.jqlInitializer = jqlInitializer;
        this.jqlFunctionServiceImpl = jqlFunctionServiceImpl;
    }

    private enum SystemPhase {
        SPRING_STARTED,
        PLUGIN_ENABLED,
        LIFECYCLE_AWARE_STARTED
    }

    private final Set<SystemPhase> systemPhases = Collections.synchronizedSet(new HashSet<SystemPhase>(SystemPhase.values().length));

    private final LazyReference<FullSystemInitializer> fullSystemInitializer = new LazyReference<FullSystemInitializer>() {
        @Override
        protected FullSystemInitializer create() {
            return new FullSystemInitializer();
        }
    };

    /**
     * First event in the lifecycle: Spring context comes up.
     */
    @PostConstruct
    public void onSpringContextStarted() {
        eventPublisher.register(this);

        onSystemStartPhase(SystemPhase.SPRING_STARTED);
    }

    /**
     * Second event in the lifecycle: System runs (this comes after the plugin framework events)
     */
    @Override
    public void onStart() {
        onSystemStartPhase(SystemPhase.LIFECYCLE_AWARE_STARTED);
    }

    @EventListener
    public void onPluginStarted(final PluginEnabledEvent pluginEnabledEvent) {
        if (pluginEnabledEvent.getPlugin().getKey().equals(Const.PLUGIN_KEY)) {
            onSystemStartPhase(SystemPhase.PLUGIN_ENABLED);
        }
    }

    private void onSystemStartPhase(final SystemPhase newPhase) {
        systemPhases.add(newPhase);
        logger.debug("phase {} completed", newPhase);

        if (systemPhases.size() == SystemPhase.values().length) {
            // all systems go
            fullSystemInitializer.get();
        }
    }

    //todo: stop
    @Override
    public void onStop() {}

    @PreDestroy
    public void onSpringContextStopped() {
        eventListenerInvoker.onStop();
        scheduledTaskService.onStop();
        executionDeletionScheduler.onStop();
        jqlInitializer.onStop();
        jqlFunctionServiceImpl.onStop();

        eventPublisher.unregister(this);

        logger.info("Plugin stopped");
    }

    private class FullSystemInitializer {
        private FullSystemInitializer() {
            eventListenerInvoker.onStart();
            scheduledTaskService.onStart();
            executionDeletionScheduler.onStart();
            jqlInitializer.onStart();
            jqlFunctionServiceImpl.onStart();

            logger.info("Plugin initialized");
        }
    }
}
