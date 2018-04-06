package ru.mail.jira.plugins.groovy.util;

public class RestFieldException extends IllegalArgumentException {
    private final String field;

    public RestFieldException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return this.field;// 12
    }
}
