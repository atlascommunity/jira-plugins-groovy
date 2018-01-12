package ru.mail.jira.plugins.groovy.impl.listener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mail.jira.plugins.groovy.impl.listener.condition.EventCondition;

@Getter
@AllArgsConstructor
public class ScriptedEventListener {
    private final int id;
    private final String script;
    private final String uuid;
    private final EventCondition condition;

    public boolean passesCondition(Object event) {
        return condition.passesCondition(event);
    }
}
