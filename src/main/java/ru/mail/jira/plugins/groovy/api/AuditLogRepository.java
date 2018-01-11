package ru.mail.jira.plugins.groovy.api;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.AuditLogEntryDto;
import ru.mail.jira.plugins.groovy.api.dto.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.Page;

public interface AuditLogRepository {
    void create(ApplicationUser user, AuditLogEntryForm form);

    Page<AuditLogEntryDto> getPagedEntries(int offset, int limit);
}
