package ru.mail.jira.plugins.groovy.api.service;

import java.util.Optional;

public interface PluginDataService {
    boolean isSentryEnabled();
    void setSentryEnabled();

    String getSentryDsnValue();
    void setSentryDsn();

    Optional<String> getSentryDsn();
}
