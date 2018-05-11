package ru.mail.jira.plugins.groovy.impl.sentry;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.cluster.ClusterInfo;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import io.sentry.Sentry;
import io.sentry.event.EventBuilder;
import io.sentry.event.User;
import io.sentry.event.UserBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.script.PluginModule;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.PluginDataService;
import ru.mail.jira.plugins.groovy.api.service.SentryService;

import java.util.Map;

@Component
@ExportAsService(LifecycleAware.class)
public class SentryServiceImpl implements SentryService, LifecycleAware {
    private static final String LOCK_KEY = "ru.mail.jira.groovy.sentry";

    private final ClusterLockService clusterLockService;
    private final ClusterInfo clusterInfo;
    private final PluginDataService pluginDataService;

    @Autowired
    public SentryServiceImpl(
        @PluginModule ClusterLockService clusterLockService,
        @PluginModule ClusterInfo clusterInfo,
        PluginDataService pluginDataService
    ) {
        this.clusterLockService = clusterLockService;
        this.clusterInfo = clusterInfo;
        this.pluginDataService = pluginDataService;
    }

    @Override
    public void registerException(
        ApplicationUser user,
        Exception e,
        ScriptType type, Integer id, String inlineId,
        String issue, Map<String, String> metaData
    ) {
        if (pluginDataService.isSentryEnabled()) {
            EventBuilder eventBuilder = new EventBuilder()
                .withExtra("scriptType", type.name())
                .withExtra("id", id != null ? id : inlineId)
                .withTag("scriptType", type.name())
                .withSentryInterface(new ExceptionInterface(e));

            if (user != null) {
                eventBuilder.withTag("user", user.getName());
            }

            if (clusterInfo.isClustered()) {
                eventBuilder.withTag("node", clusterInfo.getNodeId());
            }

            if (metaData != null) {
                metaData.forEach((k, v) -> eventBuilder.withExtra("meta." + k, v));
            }

            if (issue != null) {
                eventBuilder.withTag("issue", issue);
            }

            String scriptId = id != null ? id.toString() : inlineId;

            eventBuilder.withTag("script", type + "-" + scriptId);

            Sentry.capture(eventBuilder.build());

            Sentry.clearContext();
        }
    }

    @Override
    public void updateSettings(boolean enabled, String dsn) {
        ClusterLock lock = clusterLockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            //todo
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onStart() {
        if (pluginDataService.isSentryEnabled()) {
            Sentry.init(pluginDataService.getSentryDsn());
        }
    }

    @Override
    public void onStop() {
        Sentry.close();
    }
}
