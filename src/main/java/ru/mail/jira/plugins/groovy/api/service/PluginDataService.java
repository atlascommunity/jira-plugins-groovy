package ru.mail.jira.plugins.groovy.api.service;

public interface PluginDataService {
    boolean isSentryEnabled();
    void setSentryEnabled(boolean enabled);

    String getSentryDsn();
    void setSentryDsn(String dsn);
}
