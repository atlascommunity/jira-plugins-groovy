package ru.mail.jira.plugins.groovy.api;

import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import java.util.Map;

public interface ScriptService {
    Object executeScript(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings) throws Exception;

    void validateScript(String script);

    void invalidate(String id);

    void invalidateAll();
}
