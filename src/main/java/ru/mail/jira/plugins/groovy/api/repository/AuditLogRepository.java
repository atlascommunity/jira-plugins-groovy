package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryDto;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.Page;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;

import java.util.Set;

public interface AuditLogRepository {
    void create(ApplicationUser user, AuditLogEntryForm form);

    Page<AuditLogEntryDto> getPagedEntries(int offset, int limit, Set<String> users, Set<EntityType> categories, Set<EntityAction> actions);
}
