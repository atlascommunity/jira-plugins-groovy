package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.PluginLauncher;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;


//we use approach like in com.atlassian.greenhopper.Launcher
@Component
@ExportAsService({ PluginLauncher.class, LifecycleAware.class })
public final class PluginLauncherImpl implements LifecycleAware, PluginLauncher {
    private final Logger logger = LoggerFactory.getLogger(PluginLauncherImpl.class);

    private final EventPublisher eventPublisher;
    private final List<PluginLifecycleAware> pluginLifecycleAwareObjects;

    private volatile boolean initialized = false;

    @Autowired
    public PluginLauncherImpl(
        @ComponentImport EventPublisher eventPublisher,
        List<PluginLifecycleAware> pluginLifecycleAwareObjects
    ) {
        this.eventPublisher = eventPublisher;
        this.pluginLifecycleAwareObjects = pluginLifecycleAwareObjects
            .stream()
            .sorted(Comparator.comparingInt(PluginLifecycleAware::getInitOrder))
            .collect(Collectors.toList());
    }

    private enum SystemPhase {
        SPRING_STARTED,
        PLUGIN_ENABLED,
        LIFECYCLE_AWARE_STARTED
    }

    private final Set<SystemPhase> systemPhases = Collections.synchronizedSet(new HashSet<>(SystemPhase.values().length));

    private final LazyReference<FullSystemInitializer> fullSystemInitializer = new LazyReference<FullSystemInitializer>() {
        @Override
        protected FullSystemInitializer create() {
            return new FullSystemInitializer();
        }
    };

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public List<PluginLifecycleAware> getLifecycleAwareObjects() {
        return pluginLifecycleAwareObjects;
    }

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
        eventPublisher.unregister(this);

        for (PluginLifecycleAware object : Lists.reverse(pluginLifecycleAwareObjects)) {
            logger.info("stopping {}", object);
            object.onStop();
        }

        logger.info("Plugin stopped");
    }

    private class FullSystemInitializer {
        private FullSystemInitializer() {
            for (PluginLifecycleAware object : pluginLifecycleAwareObjects) {
                logger.info("starting {}", object);
                object.onStart();
            }

            logger.info("Plugin initialized");

            initialized = true;
        }
    }
}
