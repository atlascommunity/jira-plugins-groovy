package ru.mail.jira.plugins.groovy.api.service;

public interface PluginDataService {
    boolean isSentryEnabled();
    void setSentryEnabled();

    String getSentryDsn();
    void setSentryDsn();
}
