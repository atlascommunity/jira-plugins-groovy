package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import com.atlassian.jira.util.MessageSet;

public class JqlFunctionValidationException extends RuntimeException {
    public JqlFunctionValidationException(MessageSet messageSet) {
        super(String.join("\n", messageSet.getErrorMessages()));
    }
}
