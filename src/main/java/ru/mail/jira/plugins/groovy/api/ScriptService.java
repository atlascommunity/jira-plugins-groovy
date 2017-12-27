package ru.mail.jira.plugins.groovy.api;

import java.util.Map;

public interface ScriptService {
    Object executeScript(String scriptId, String scriptString, Map<String, Object> bindings) throws Exception;

    void validateScript(String script);

    void invalidate(String id);
}