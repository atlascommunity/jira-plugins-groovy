package ru.mail.jira.plugins.groovy.api.dto.workflow;

import lombok.Getter;
import ru.mail.jira.plugins.groovy.util.Const;

@Getter
public enum WorkflowScriptType {
    CONDITION(Const.REGISTRY_CONDITION_KEY),
    VALIDATOR(Const.REGISTRY_VALIDATOR_KEY),
    FUNCTION(Const.REGISTRY_FUNCTION_KEY);

    private String moduleKey;

    WorkflowScriptType(String moduleKey) {
        this.moduleKey = moduleKey;
    }
}
