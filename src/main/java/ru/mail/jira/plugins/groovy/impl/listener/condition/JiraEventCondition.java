package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.atlassian.jira.event.issue.IssueEvent;

public abstract class JiraEventCondition implements EventCondition {
    @Override
    public boolean passesCondition(Object event) {
        return event instanceof IssueEvent && passesCondition(((IssueEvent) event));
    }

    protected abstract boolean passesCondition(IssueEvent event);
}
