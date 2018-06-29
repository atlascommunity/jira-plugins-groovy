package ru.mail.jira.plugins.groovy.impl.jql;

import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;
import ru.mail.jira.plugins.groovy.api.repository.JqlFunctionRepository;

@Component
@ExportAsService(LifecycleAware.class)
public class ModuleReplicationService implements LifecycleAware, ClusterMessageConsumer {
    private static final String JQL_CHANNEL = "ru.mail.groovy.jql";

    private final Logger logger = LoggerFactory.getLogger(ModuleReplicationService.class);

    private final ClusterMessagingService clusterMessagingService;
    private final JqlFunctionRepository repository;
    private final ModuleManager moduleManager;

    @Autowired
    public ModuleReplicationService(
        @ComponentImport ClusterMessagingService clusterMessagingService,
        JqlFunctionRepository repository,
        ModuleManager moduleManager
    ) {
        this.clusterMessagingService = clusterMessagingService;
        this.repository = repository;
        this.moduleManager = moduleManager;
    }

    @Override
    public void onStart() {
        clusterMessagingService.registerListener(JQL_CHANNEL, this);
    }

    @Override
    public void onStop() {
        clusterMessagingService.unregisterListener(JQL_CHANNEL, this);
    }

    public void registerModule(int id) {
        clusterMessagingService.sendRemote(JQL_CHANNEL, "R" + id);
    }

    public void unregisterModule(int id) {
        clusterMessagingService.sendRemote(JQL_CHANNEL, "U" + id);
    }

    @Override
    public void receive(String channel, String message, String senderId) {
        if (JQL_CHANNEL.equals(channel)) {
            if (message.startsWith("R")) {
                Integer id = Ints.tryParse(message.substring(1));
                if (id != null) {
                    JqlFunctionScriptDto script = repository.getScript(id);
                    if (script != null) {
                        moduleManager.registerScript(script);
                    } else {
                        logger.error("Unable to register script with id {} because it was not found", id);
                    }
                } else {
                    logger.error("unable to parse id from {}", message);
                }
            } else if (message.startsWith("U")) {
                Integer id = Ints.tryParse(message.substring(1));
                if (id != null) {
                    moduleManager.unregisterScript(id);
                } else {
                    logger.error("unable to parse id from {}", message);
                }
            } else {
                logger.warn("Unsupported message {}", message);
            }
        }
    }
}
