package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

//todo: migrate everything to ValidationException
@Getter
public class ValidationException extends RuntimeException {
    private final List<String> messages;
    private final String field;

    public ValidationException(List<String> messages, String field) {
        super(messages.stream().collect(Collectors.joining("; ")));
        this.messages = messages;
        this.field = field;
    }

    public ValidationException(String message, String field) {
        this(ImmutableList.of(message), field);
    }

    public ValidationException(String message) {
        this(message, null);
    }

    public ValidationException(List<String> messages) {
        this(messages, null);
    }
}
