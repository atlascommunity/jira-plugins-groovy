package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.function.Consumer;

@Named
public class IssueHelper {
    @ComponentImport
    @Inject
    private IssueFactory issueFactory;

    @ComponentImport
    @Inject
    private IssueManager issueManager;

    public Issue createIssue(ApplicationUser reporter, Project project) throws CreateException {
        return createIssue(reporter, project, issue -> {});
    }

    public Issue createIssue(ApplicationUser reporter, Project project, Consumer<MutableIssue> customizer) throws CreateException {
        MutableIssue issue = issueFactory.getIssue();
        issue.setIssueType(project.getIssueTypes().iterator().next());
        issue.setSummary("TEST ISSUE");
        issue.setReporter(reporter);
        issue.setProjectObject(project);

        customizer.accept(issue);

        return issueManager.createIssueObject(reporter, issue);
    }
}
