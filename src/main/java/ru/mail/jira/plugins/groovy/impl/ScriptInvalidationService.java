package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.ScriptService;

@Component
@ExportAsService(LifecycleAware.class)
public class ScriptInvalidationService implements LifecycleAware {
    private static final String INVALIDATION_CHANNEL = "ru.mail.groovy.si";

    private final Logger logger = LoggerFactory.getLogger(ScriptInvalidationService.class);
    private final ClusterMessagingService clusterMessagingService;
    private final ScriptService scriptService;
    private final MessageConsumer messageConsumer;

    @Autowired
    public ScriptInvalidationService(
        @ComponentImport ClusterMessagingService clusterMessagingService,
        ScriptService scriptService
    ) {
        this.clusterMessagingService = clusterMessagingService;
        this.scriptService = scriptService;
        this.messageConsumer = new MessageConsumer();
    }

    public void invalidate(String scriptId) {
        logger.debug("sending invalidation message for {}", scriptId);
        clusterMessagingService.sendRemote(INVALIDATION_CHANNEL, scriptId);
        scriptService.invalidate(scriptId);
    }

    public void invalidateAll() {
        logger.debug("sending invalidation message for all");
        clusterMessagingService.sendRemote(INVALIDATION_CHANNEL, "");
        scriptService.invalidateAll();
    }

    @Override
    public void onStart() {
        this.clusterMessagingService.registerListener(INVALIDATION_CHANNEL, this.messageConsumer);
    }

    @Override
    public void onStop() {
        this.clusterMessagingService.unregisterListener(INVALIDATION_CHANNEL, this.messageConsumer);
    }

    private class MessageConsumer implements ClusterMessageConsumer {
        @Override
        public void receive(String channel, String message, String senderId) {
            if (INVALIDATION_CHANNEL.equals(channel)) {
                if (StringUtils.isEmpty(message)) {
                    scriptService.invalidateAll();
                } else {
                    scriptService.invalidate(message);
                }
                logger.debug("invalidating {}", message);
            }
        }
    }
}
