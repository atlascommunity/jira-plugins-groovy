package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import lombok.Getter;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.messages.Message;

import java.io.PrintWriter;

@Getter
public class WarningMessage extends Message {
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

    @Override
    public void write(PrintWriter writer, Janitor janitor) {

    }
}
