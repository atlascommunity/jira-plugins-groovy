package ru.mail.jira.plugins.groovy.api;

import ru.mail.jira.plugins.groovy.api.dto.RestScriptDto;

public interface RestRepository {
    RestScriptDto getScript(String method, String key);
}
