package ru.mail.jira.plugins.groovy.impl.cf;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class ValueHolder<T> {
    private final long lastModified;
    private final T value;
    private final Map<String, Object> velocityParams;
}
