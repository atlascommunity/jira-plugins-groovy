package ru.mail.jira.plugins.groovy.api.service;

import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;

import java.util.Map;

public interface ScriptService {
    Object executeScript(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings) throws Exception;

    ParseContext parseScript(String script);

    void invalidate(String id);

    void invalidateAll();
}
