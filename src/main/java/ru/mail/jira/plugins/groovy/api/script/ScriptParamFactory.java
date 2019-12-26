package ru.mail.jira.plugins.groovy.api.script;

import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

public interface ScriptParamFactory {
    Object getParamObject(ScriptParamDto paramDto, String value);

    Object getParamFormValue(ScriptParamDto paramDto, String value);
}
