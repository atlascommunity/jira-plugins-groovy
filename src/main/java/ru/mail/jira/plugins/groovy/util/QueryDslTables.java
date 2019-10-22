package ru.mail.jira.plugins.groovy.util;

import ru.mail.jira.plugins.groovy.impl.repository.querydsl.*;

public final class QueryDslTables {
    private QueryDslTables() {}

    public static final QAuditLogEntry AUDIT_LOG_ENTRY = new QAuditLogEntry();
    public static final QAuditLogIssueRelation AUDIT_LOG_ISSUE_RELATION = new QAuditLogIssueRelation();
    public static final QScriptExecution SCRIPT_EXECUTION = new QScriptExecution();

    public static final QAbstractScript ADMIN_SCRIPT = new QAbstractScript("ADMIN_SCRIPT");
    public static final QAbstractScript JQL_FUNCTION = new QAbstractScript("JQL_FUNCTION_SCRIPT");
    public static final QAbstractScript LISTENER = new QAbstractScript("LISTENER");
    public static final QAbstractScript REST = new QAbstractScript("REST_SCRIPT");
    public static final QAbstractScript SCHEDULED_TASK = new QAbstractScript("SCHEDULED_TASK");
    public static final QAbstractScript REGISTRY_SCRIPT = new QAbstractScript("SCRIPT");
    public static final QFieldConfig FIELD_CONFIG = new QFieldConfig();

    public static final QAbstractChangelog ADMIN_CHANGELOG = new QAbstractChangelog("A_SCRIPT_CHANGELOG");
    public static final QAbstractChangelog JQL_CHANGELOG = new QAbstractChangelog("JQL_CHANGELOG");
    public static final QAbstractChangelog LISTENER_CHANGELOG = new QAbstractChangelog("LISTENER_CHANGELOG", "LISTENER_ID");
    public static final QAbstractChangelog REST_CHANGELOG = new QAbstractChangelog("REST_CHANGELOG");
    public static final QAbstractChangelog SCHEDULED_CHANGELOG = new QAbstractChangelog("S_TASK_CHANGELOG", "TASK_ID");
    public static final QAbstractChangelog SCRIPT_CHANGELOG = new QAbstractChangelog("CHANGELOG");
    public static final QAbstractChangelog FIELD_CHANGELOG = new QAbstractChangelog("FIELD_CHANGELOG", "FIELD_CONFIG_ID");
}
