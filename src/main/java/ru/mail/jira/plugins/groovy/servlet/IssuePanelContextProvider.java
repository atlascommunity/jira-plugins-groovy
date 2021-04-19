package ru.mail.jira.plugins.groovy.servlet;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;

import java.util.Collections;
import java.util.Map;

public class IssuePanelContextProvider extends AbstractJiraContextProvider {
    private final AuditLogRepository auditLogRepository;

    public IssuePanelContextProvider(
        AuditLogRepository auditLogRepository
    ) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        Map<String, Object> params = jiraHelper.getContextParams();

        Issue issue = (Issue) params.get("issue");

        if (issue == null) {
            return ImmutableMap.of("changes", Collections.emptyList());
        }

        return ImmutableMap.of(
            "changes", auditLogRepository.getRelated(issue.getId())
        );
    }
}
