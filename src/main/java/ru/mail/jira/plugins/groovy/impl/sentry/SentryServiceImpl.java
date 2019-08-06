package ru.mail.jira.plugins.groovy.impl.sentry;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.cluster.ClusterInfo;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.sentry.Sentry;
import io.sentry.event.EventBuilder;
import io.sentry.event.User;
import io.sentry.event.interfaces.ExceptionInterface;
import io.sentry.event.interfaces.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.PluginDataService;
import ru.mail.jira.plugins.groovy.api.service.SentryService;
import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;

import java.util.Map;
import java.util.Optional;

@Component
public class SentryServiceImpl implements SentryService, PluginLifecycleAware {
    private static final String LOCK_KEY = "ru.mail.jira.groovy.sentry";

    private final ClusterLockService clusterLockService;
    private final ClusterInfo clusterInfo;
    private final PluginDataService pluginDataService;

    @Autowired
    public SentryServiceImpl(
        @ComponentImport ClusterLockService clusterLockService,
        @ComponentImport ClusterInfo clusterInfo,
        PluginDataService pluginDataService
    ) {
        this.clusterLockService = clusterLockService;
        this.clusterInfo = clusterInfo;
        this.pluginDataService = pluginDataService;
    }

    @Override
    public void registerException(
        String id,
        User user,
        Exception e,
        ScriptType type,
        String issue,
        Map<String, String> metaData
    ) {
        if (pluginDataService.isSentryEnabled()) {
            EventBuilder eventBuilder = new EventBuilder()
                .withTag("id", id)
                .withSentryInterface(new ExceptionInterface(e))
                .withSentryInterface(new UserInterface(
                    user.getId(), user.getUsername(), user.getIpAddress(), user.getEmail(), user.getData()
                ));

            if (clusterInfo.isClustered()) {
                eventBuilder.withTag("node", clusterInfo.getNodeId());
            }

            if (metaData != null) {
                metaData.forEach((k, v) -> eventBuilder.withExtra("meta." + k, v));
            }

            if (issue != null) {
                eventBuilder.withTag("issue", issue);
            }

            eventBuilder.withTag("script", type + "-" + id);

            Sentry.capture(eventBuilder.build());

            Sentry.clearContext();
        }
    }

    @Override
    public void updateSettings(boolean enabled, String dsn) {
        ClusterLock lock = clusterLockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            pluginDataService.setSentryEnabled(enabled);
            pluginDataService.setSentryDsn(dsn);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void invalidateSettings() {
        init();
    }

    @Override
    public void onStart() {
        init();
    }

    @Override
    public void onStop() {
        Sentry.close();
    }

    @Override
    public int getInitOrder() {
        return 0;
    }

    private void init() {
        Optional<String> dsn = getDsn();
        if (dsn.isPresent()) {
            Sentry.init(dsn.get());
        } else {
            Sentry.close();
        }
    }

    private Optional<String> getDsn() {
        return pluginDataService.getSentryDsn();
    }
}
