package ru.mail.jira.plugins.groovy.api.service;

import io.sentry.event.User;
import io.sentry.event.interfaces.HttpInterface;

import java.util.Map;

public interface SentryService {
    void registerException(String id, User user, Throwable e, HttpInterface httpInterface, Map<String, String> metaData);

    void updateSettings(boolean enabled, String dsn);

    boolean isEnabled();
    String getDsn();

    void invalidateSettings();
}
