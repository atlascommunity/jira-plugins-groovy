package ru.mail.jira.plugins.groovy.api.entity;

import lombok.Getter;

public enum EntityType {
    REGISTRY_SCRIPT("ru.mail.jira.plugins.groovy.audit.registryScript"),
    REGISTRY_DIRECTORY("ru.mail.jira.plugins.groovy.audit.registryDirectory"),
    LISTENER("ru.mail.jira.plugins.groovy.audit.listener"),
    REST("ru.mail.jira.plugins.groovy.audit.rest"),
    CUSTOM_FIELD("ru.mail.jira.plugins.groovy.audit.cf"),
    SCHEDULED_TASK("ru.mail.jira.plugins.groovy.audit.scheduledTask"),
    ADMIN_SCRIPT("ru.mail.jira.plugins.groovy.audit.adminScript");

    @Getter
    private final String i18nName;

    EntityType(String i18nName) {
        this.i18nName = i18nName;
    }
}
