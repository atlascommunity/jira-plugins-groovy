package ru.mail.jira.plugins.groovy.impl.listener.condition;

public interface EventCondition {
    boolean passesCondition(Object event);
}
