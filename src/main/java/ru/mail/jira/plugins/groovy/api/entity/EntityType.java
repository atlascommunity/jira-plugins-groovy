package ru.mail.jira.plugins.groovy.api.entity;

import lombok.Getter;

public enum EntityType {
    REGISTRY_SCRIPT("ru.mail.jira.plugins.groovy.audit.registryScript", true),
    REGISTRY_DIRECTORY("ru.mail.jira.plugins.groovy.audit.registryDirectory", false),
    LISTENER("ru.mail.jira.plugins.groovy.audit.listener", true),
    REST("ru.mail.jira.plugins.groovy.audit.rest", true),
    CUSTOM_FIELD("ru.mail.jira.plugins.groovy.audit.cf", true),
    SCHEDULED_TASK("ru.mail.jira.plugins.groovy.audit.scheduledTask", true),
    ADMIN_SCRIPT("ru.mail.jira.plugins.groovy.audit.adminScript", true),
    JQL_FUNCTION("ru.mail.jira.plugins.groovy.audit.jql", true);

    @Getter
    private final String i18nName;
    @Getter
    private final boolean supportsPermalink;

    EntityType(String i18nName, boolean supportsPermalink) {
        this.i18nName = i18nName;
        this.supportsPermalink = supportsPermalink;
    }
}
