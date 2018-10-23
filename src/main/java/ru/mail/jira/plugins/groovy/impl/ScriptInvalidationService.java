package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisablingEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.google.common.primitives.Longs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.cf.FieldValueCache;
import ru.mail.jira.plugins.groovy.impl.groovy.var.GlobalObjectsBindingProvider;

@Component
@ExportAsService(LifecycleAware.class)
public class ScriptInvalidationService implements LifecycleAware {
    private static final String SCRIPT_INVALIDATION_CHANNEL = "ru.mail.groovy.si";
    private static final String FIELD_INVALIDATION_CHANNEL = "ru.mail.groovy.fi";
    private static final String GLOBAL_OBJECTS_CHANNEL = "ru.mail.groovy.go";

    private final Logger logger = LoggerFactory.getLogger(ScriptInvalidationService.class);
    private final ClusterMessagingService clusterMessagingService;
    private final PluginEventManager pluginEventManager;
    private final ScriptService scriptService;
    private final FieldValueCache fieldValueCache;
    private final MessageConsumer messageConsumer;
    private final GlobalObjectsBindingProvider globalObjectsBindingProvider;

    @Autowired
    public ScriptInvalidationService(
        @ComponentImport ClusterMessagingService clusterMessagingService,
        @ComponentImport PluginEventManager pluginEventManager,
        ScriptService scriptService,
        FieldValueCache fieldValueCache,
        GlobalObjectsBindingProvider globalObjectsBindingProvider
    ) {
        this.clusterMessagingService = clusterMessagingService;
        this.pluginEventManager = pluginEventManager;
        this.scriptService = scriptService;
        this.fieldValueCache = fieldValueCache;
        this.globalObjectsBindingProvider = globalObjectsBindingProvider;
        this.messageConsumer = new MessageConsumer();
    }

    public void invalidate(String scriptId) {
        logger.debug("sending invalidation message for {}", scriptId);
        clusterMessagingService.sendRemote(SCRIPT_INVALIDATION_CHANNEL, scriptId);
        scriptService.invalidate(scriptId);
    }

    public void invalidateAll() {
        logger.debug("sending invalidation message for all");
        clusterMessagingService.sendRemote(SCRIPT_INVALIDATION_CHANNEL, "");
        scriptService.invalidateAll();
    }

    public void invalidateField(long fieldId) {
        logger.debug("sending invalidation message for field {}", fieldId);
        clusterMessagingService.sendRemote(FIELD_INVALIDATION_CHANNEL, String.valueOf(fieldId));
        fieldValueCache.invalidateField(fieldId);
    }

    public void invalidateAllFields() {
        logger.debug("sending invalidation message for all fields");
        clusterMessagingService.sendRemote(FIELD_INVALIDATION_CHANNEL, "");
        fieldValueCache.invalidateAll();
    }

    //todo: возможно, можно без сильной боли инвалидировать только конкретный скрипт (есть ли в этом смысл?)
    public void invalidateGlobalObjects() {
        clusterMessagingService.sendRemote(GLOBAL_OBJECTS_CHANNEL, "");
        globalObjectsBindingProvider.refresh();
    }

    @Override
    public void onStart() {
        this.clusterMessagingService.registerListener(SCRIPT_INVALIDATION_CHANNEL, this.messageConsumer);
        this.clusterMessagingService.registerListener(FIELD_INVALIDATION_CHANNEL, this.messageConsumer);
        this.clusterMessagingService.registerListener(GLOBAL_OBJECTS_CHANNEL, this.messageConsumer);

        pluginEventManager.register(this);
    }

    @Override
    public void onStop() {
        this.clusterMessagingService.unregisterListener(SCRIPT_INVALIDATION_CHANNEL, this.messageConsumer);
        this.clusterMessagingService.unregisterListener(FIELD_INVALIDATION_CHANNEL, this.messageConsumer);
        this.clusterMessagingService.unregisterListener(GLOBAL_OBJECTS_CHANNEL, this.messageConsumer);

        pluginEventManager.unregister(this);
    }

    @EventListener
    public void onPluginUnloading(PluginDisablingEvent event) {
        scriptService.onPluginUnloading(event);
        globalObjectsBindingProvider.refresh();
        //todo: invalidate listeners
    }

    private class MessageConsumer implements ClusterMessageConsumer {
        @Override
        public void receive(String channel, String message, String senderId) {
            if (SCRIPT_INVALIDATION_CHANNEL.equals(channel)) {
                if (StringUtils.isEmpty(message)) {
                    scriptService.invalidateAll();
                } else {
                    scriptService.invalidate(message);
                }
                logger.debug("invalidating {}", message);
            } else if (FIELD_INVALIDATION_CHANNEL.equals(channel)) {
                if (StringUtils.isEmpty(message)) {
                    fieldValueCache.invalidateAll();
                } else {
                    Long fieldId = Longs.tryParse(message);
                    if (fieldId != null) {
                        fieldValueCache.invalidateField(fieldId);
                    } else {
                        logger.error("unable to parse field id {}", message);
                    }
                }
            } else if (GLOBAL_OBJECTS_CHANNEL.equals(channel)) {
                globalObjectsBindingProvider.refresh();
            }
        }
    }
}
