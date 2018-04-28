package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.user.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.notification.NotificationDto;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.service.NotificationService;
import ru.mail.jira.plugins.groovy.api.service.WatcherService;

import java.util.List;

@Component
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final WatcherService watcherService;

    @Autowired
    public AuditService(
        AuditLogRepository auditLogRepository,
        NotificationService notificationService,
        WatcherService watcherService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
        this.watcherService = watcherService;
    }

    public void addAuditLogAndNotify(
        ApplicationUser user, EntityAction action,
        EntityType entityType, AbstractScript script, String diff,
        String description
    ) {
        addAuditLogAndNotify(
            user, action,
            entityType, script.getID(), script.getName(),
            diff, null, description
        );
    }

    public void addAuditLogAndNotify(
        ApplicationUser user, EntityAction action,
        EntityType entityType, AbstractScript script, String diff,
        String description, List<ApplicationUser> watchers
    ) {
        addAuditLogAndNotify(
            user, action,
            entityType, script.getID(), script.getName(),
            diff, null, description, watchers
        );
    }

    public void addAuditLogAndNotify(
        ApplicationUser user, EntityAction action,
        EntityType entityType, int entityId, String entityName,
        String diff, String templateDiff, String description
    ) {
        addAuditLogAndNotify(
            user, action,
            entityType, entityId, entityName,
            diff, templateDiff, description,
            watcherService.getWatchers(entityType, entityId)
        );
    }

    public void addAuditLogAndNotify(
        ApplicationUser user, EntityAction action,
        EntityType entityType, int entityId, String entityName,
        String diff, String templateDiff, String description,
        List<ApplicationUser> watchers
    ) {
        if (action == EntityAction.CREATED) {
            watcherService.addWatcher(entityType, entityId, user);
        }

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                entityType,
                entityId,
                action,
                description
            )
        );

        notificationService.sendNotifications(
            new NotificationDto(
                user, action, entityType, entityName, entityId, diff, templateDiff, description
            ),
            watchers
        );
    }
}
