package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Table("AUDIT_ISSUE_REL")
public interface AuditLogIssueRelation extends Entity {
    @NotNull
    AuditLogEntry getAuditLog();

    @NotNull
    @Indexed
    Long getIssueId();
}
