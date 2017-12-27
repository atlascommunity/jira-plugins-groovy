package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.atlassian.jira.event.issue.IssueEvent;
import com.google.common.base.Preconditions;

import java.util.Set;

public class ProjectCondition extends JiraEventCondition {
    private final Set<Long> projectIds;

    public ProjectCondition(Set<Long> projectIds) {
        Preconditions.checkNotNull(projectIds);

        this.projectIds = projectIds;
    }

    @Override
    protected boolean passesCondition(IssueEvent issueEvent) {
        Long projectId = issueEvent.getIssue().getProjectId();

        return projectId != null && projectIds.contains(projectId);

    }
}
