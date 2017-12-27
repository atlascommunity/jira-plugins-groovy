package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.google.common.base.Preconditions;

public class ClassNameEventCondition implements EventCondition {
    private final Class eventClass;

    public ClassNameEventCondition(String className) {
        Preconditions.checkNotNull(className);

        try {
            this.eventClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load class for condition", e);
        }
    }

    @Override
    public boolean passesCondition(Object event) {
        return eventClass.isInstance(event);
    }
}
