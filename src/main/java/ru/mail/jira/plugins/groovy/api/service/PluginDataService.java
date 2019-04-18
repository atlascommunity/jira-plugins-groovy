package ru.mail.jira.plugins.groovy.api.service;

import java.util.Optional;

public interface PluginDataService {
    boolean isSentryEnabled();
    void setSentryEnabled(boolean enabled);

    String getSentryDsnValue();
    void setSentryDsn(String dsn);

    Optional<String> getSentryDsn();
}
