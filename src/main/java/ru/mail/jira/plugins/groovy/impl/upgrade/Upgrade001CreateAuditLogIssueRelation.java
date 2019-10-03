package ru.mail.jira.plugins.groovy.impl.upgrade;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.entity.AuditLogEntry;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.Collection;
import java.util.Collections;

@Component
@ExportAsService(PluginUpgradeTask.class)
public class Upgrade001CreateAuditLogIssueRelation implements PluginUpgradeTask {
    private final ActiveObjects activeObjects;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public Upgrade001CreateAuditLogIssueRelation(
        @ComponentImport ActiveObjects activeObjects,
        AuditLogRepository auditLogRepository
    ) {
        this.activeObjects = activeObjects;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public int getBuildNumber() {
        return 100;
    }

    @Override
    public String getShortDescription() {
        return "Creates relation between audit log entry and Jira issues";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        for (AuditLogEntry auditLogEntry : activeObjects.find(AuditLogEntry.class)) {
            auditLogRepository.createRelations(auditLogEntry);
        }

        return Collections.emptyList();
    }

    @Override
    public String getPluginKey() {
        return Const.PLUGIN_KEY;
    }
}
