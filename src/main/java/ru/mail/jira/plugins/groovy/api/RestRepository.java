package ru.mail.jira.plugins.groovy.api;

import ru.mail.jira.plugins.groovy.api.dto.RestScriptDescription;

public interface RestRepository {

    RestScriptDescription getScript(String method, String key);
}
