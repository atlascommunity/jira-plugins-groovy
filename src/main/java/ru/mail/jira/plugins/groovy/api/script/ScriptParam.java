package ru.mail.jira.plugins.groovy.api.script;

import java.util.Map;

public interface ScriptParam {
    Object runScript(Map<String, Object> params) throws Exception;
}
