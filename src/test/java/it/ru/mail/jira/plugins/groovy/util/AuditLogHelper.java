package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryDto;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Named
public class AuditLogHelper {
    @Inject
    @ComponentImport
    private AuditLogRepository auditLogRepository;

    public void assertAuditLogCreated(int id, EntityType entityType, EntityAction action) {
        assertAuditLogCreated(id, id, entityType, action);
    }

    public void assertAuditLogCreated(int id, long scriptId, EntityType entityType, EntityAction action) {
        List<AuditLogEntryDto> auditLogs = auditLogRepository.findAllForEntity(id, entityType);

        assertTrue(auditLogs.size() > 0);

        AuditLogEntryDto lastItem = auditLogs.get(auditLogs.size() - 1);

        assertNotNull(lastItem);
        assertEquals(scriptId, (long) lastItem.getScriptId());
        assertEquals(entityType, lastItem.getCategory());
        assertEquals(action, lastItem.getAction());
    }
}
