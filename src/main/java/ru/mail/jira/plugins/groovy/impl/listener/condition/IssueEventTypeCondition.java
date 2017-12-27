package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.atlassian.jira.event.issue.IssueEvent;
import com.google.common.base.Preconditions;

import java.util.Set;

public class IssueEventTypeCondition extends JiraEventCondition {
    private final Set<Long> eventTypeIds;

    public IssueEventTypeCondition(Set<Long> eventTypeIds) {
        Preconditions.checkNotNull(eventTypeIds);

        this.eventTypeIds = eventTypeIds;
    }

    @Override
    protected boolean passesCondition(IssueEvent event) {
        return event.getEventTypeId() != null && eventTypeIds.contains(event.getEventTypeId());
    }
}
