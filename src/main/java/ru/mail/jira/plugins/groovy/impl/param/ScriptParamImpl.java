package ru.mail.jira.plugins.groovy.impl.param;

import ru.mail.jira.plugins.groovy.api.script.ScriptParam;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;

import java.util.Map;

public class ScriptParamImpl implements ScriptParam {
    private final ScriptService scriptService;
    private final String body;

    public ScriptParamImpl(ScriptService scriptService, String body) {
        this.scriptService = scriptService;
        this.body = body;
    }

    @Override
    public Object runScript(Map<String, Object> params) throws Exception {
        return scriptService.executeScript(null, body, ScriptType.PARAM, params);
    }
}
