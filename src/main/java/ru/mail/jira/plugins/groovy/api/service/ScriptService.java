package ru.mail.jira.plugins.groovy.api.service;

import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;

import java.util.Map;

public interface ScriptService {
    Object executeScript(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings) throws Exception;

    Object executeScriptStatic(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings, Map<String, Class> types) throws Exception;

    ParseContext parseScript(String script);

    ParseContext parseScriptStatic(String script, Map<String, Class> types);

    Map<String, Class> getGlobalVariableTypes();

    void invalidate(String id);

    void invalidateAll();
}
