package ru.mail.jira.plugins.groovy.api.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @AllArgsConstructor
public class ScriptExecutionOutcome {
    private final Object result;
    private final ExecutionContext executionContext;
    private final long time;

    @Setter
    private Exception error;

    public boolean isSuccessful() {
        return error == null;
    }
}
