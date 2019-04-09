package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Named
public class IssueHelper {
    @ComponentImport
    @Inject
    private IssueFactory issueFactory;

    @ComponentImport
    @Inject
    private IssueManager issueManager;

    @ComponentImport
    @Inject
    private SearchService searchService;

    public MutableIssue getIssue(String key) {
        return issueManager.getIssueByCurrentKey(key);
    }

    public SearchResults search(ApplicationUser user, String queryString) throws SearchException {
        SearchService.ParseResult parseResult = searchService.parseQuery(user, queryString);
        assertTrue(parseResult.getErrors().toString(), parseResult.isValid());
        Query query = parseResult.getQuery();
        return search(user, query);
    }

    public SearchResults search(ApplicationUser user, Query query) throws SearchException {
        return searchService.searchOverrideSecurity(user, query, PagerFilter.getUnlimitedFilter());
    }

    public Issue getIssueFromIndex(ApplicationUser user, String key) throws SearchException {
        SearchResults searchResult = search(user, JqlQueryBuilder.newBuilder().where().field("key").eq(key).buildQuery());

        assertEquals(1, searchResult.getTotal());
        return searchResult.getIssues().get(0);
    }

    public void assignIssue(MutableIssue issue, ApplicationUser user, ApplicationUser assignee) {
        issue.setAssignee(assignee);
        issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false);
    }

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
