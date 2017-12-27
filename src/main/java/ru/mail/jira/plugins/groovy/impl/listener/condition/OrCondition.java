package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.google.common.base.Preconditions;

import java.util.Collection;

public class OrCondition implements EventCondition {
    private final Collection<EventCondition> children;

    public OrCondition(Collection<EventCondition> children) {
        Preconditions.checkNotNull(children);

        this.children = children;
    }

    @Override
    public boolean passesCondition(Object event) {
        return children.stream().anyMatch(condition -> condition.passesCondition(event));
    }
}
