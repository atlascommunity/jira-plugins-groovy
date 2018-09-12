package ru.mail.jira.plugins.groovy.api.script.statik;

import lombok.Getter;

@Getter
public class WarningMessage {
    private final String message;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    public WarningMessage(String message, int startLine, int startColumn, int endLine, int endColumn) {
        this.message = message;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }
}
