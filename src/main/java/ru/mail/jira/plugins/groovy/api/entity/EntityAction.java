package ru.mail.jira.plugins.groovy.api.entity;

import lombok.Getter;

public enum EntityAction {
    CREATED("ru.mail.jira.plugins.groovy.audit.action.created"),
    UPDATED("ru.mail.jira.plugins.groovy.audit.action.updated"),
    DELETED("ru.mail.jira.plugins.groovy.audit.action.deleted"),
    ENABLED("ru.mail.jira.plugins.groovy.audit.action.enabled"),
    DISABLED("ru.mail.jira.plugins.groovy.audit.action.disabled"),
    MOVED("ru.mail.jira.plugins.groovy.audit.action.moved");

    @Getter
    private final String i18nName;

    EntityAction(String i18nName) {
        this.i18nName = i18nName;
    }
}
