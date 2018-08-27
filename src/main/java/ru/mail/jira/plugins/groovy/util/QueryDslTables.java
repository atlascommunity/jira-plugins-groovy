package ru.mail.jira.plugins.groovy.util;

import ru.mail.jira.plugins.groovy.impl.repository.querydsl.QAbstractScript;
import ru.mail.jira.plugins.groovy.impl.repository.querydsl.QAuditLogEntry;
import ru.mail.jira.plugins.groovy.impl.repository.querydsl.QFieldConfig;
import ru.mail.jira.plugins.groovy.impl.repository.querydsl.QScriptExecution;

public final class QueryDslTables {
    private QueryDslTables() {}

    public static final QAuditLogEntry AUDIT_LOG_ENTRY = new QAuditLogEntry();
    public static final QScriptExecution SCRIPT_EXECUTION = new QScriptExecution();
    public static final QAbstractScript ADMIN_SCRIPT = new QAbstractScript("ADMIN_SCRIPT");
    public static final QAbstractScript JQL_FUNCTION = new QAbstractScript("JQL_FUNCTION_SCRIPT");
    public static final QAbstractScript LISTENER = new QAbstractScript("LISTENER");
    public static final QAbstractScript REST = new QAbstractScript("REST_SCRIPT");
    public static final QAbstractScript SCHEDULED_TASK = new QAbstractScript("SCHEDULED_TASK");
    public static final QAbstractScript REGISTRY_SCRIPT = new QAbstractScript("SCRIPT");
    public static final QFieldConfig FIELD_CONFIG = new QFieldConfig();
}
