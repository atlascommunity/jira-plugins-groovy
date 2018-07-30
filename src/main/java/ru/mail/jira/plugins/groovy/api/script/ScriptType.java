package ru.mail.jira.plugins.groovy.api.script;

public enum ScriptType {
    WORKFLOW_FUNCTION,
    WORKFLOW_VALIDATOR,
    WORKFLOW_CONDITION,
    WORKFLOW_GENERIC, //used for static checking
    LISTENER,
    CONSOLE,
    REST,
    CUSTOM_FIELD,
    SCHEDULED_TASK,
    ADMIN_SCRIPT,
    PARAM,
    JQL
}
