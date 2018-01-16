package ru.mail.jira.plugins.groovy.impl.listener;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScriptedEventListener {
    private final int id;
    private final String script;
    private final String uuid;
    private final ConditionDescriptor condition;
}
