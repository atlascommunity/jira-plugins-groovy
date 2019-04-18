package ru.mail.jira.plugins.groovy.api.service;

import io.sentry.event.User;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import java.util.Map;

public interface SentryService {
    void registerException(User user, Exception e, ScriptType type, Integer id, String inlineId, String issue, Map<String, String> metaData);

    void updateSettings(boolean enabled, String dsn);

    void invalidateSettings();
}
