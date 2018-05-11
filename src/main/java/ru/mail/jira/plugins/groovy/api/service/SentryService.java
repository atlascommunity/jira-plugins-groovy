package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import java.util.Map;

public interface SentryService {
    void registerException(ApplicationUser user, Exception e, ScriptType type, Integer id, String inlineId, String issue, Map<String, String> metaData);

    void updateSettings(boolean enabled, String dsn);
}
