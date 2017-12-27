package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.google.common.base.Preconditions;

import java.util.Collection;

public class AndCondition implements EventCondition {
    private final Collection<EventCondition> children;

    public AndCondition(Collection<EventCondition> children) {
        Preconditions.checkNotNull(children);

        this.children = children;
    }

    @Override
    public boolean passesCondition(Object event) {
        return children.stream().allMatch(condition -> condition.passesCondition(event));
    }
}
