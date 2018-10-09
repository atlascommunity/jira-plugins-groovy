package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.util.ValidationException;

import java.util.List;
import java.util.Map;

@Component
public class FixResolution implements BuiltInScript<String> {
    private final SearchService searchService;
    private final IssueManager issueManager;

    @Autowired
    public FixResolution(
        @ComponentImport SearchService searchService,
        @ComponentImport IssueManager issueManager
    ) {
        this.searchService = searchService;
        this.issueManager = issueManager;
    }

    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) throws Exception {
        boolean sendEmail = (Boolean) params.getOrDefault("sendEmail", false);
        String jql = StringUtils.trimToNull((String) params.get("jql"));
        Resolution resolution = (Resolution) params.get("resolution");

        if (jql == null) {
            throw new ValidationException("Query is required");
        }

        SearchService.ParseResult parseResult = searchService.parseQuery(currentUser, jql);
        if (!parseResult.isValid()) {
            throw new ValidationException(parseResult.getErrors().toString());
        }

        SearchResults searchResult = searchService.search(
            currentUser, parseResult.getQuery(), PagerFilter.newPageAlignedFilter(0, 1000)
        );

        for (Issue docIssue : searchResult.getIssues()) {
            MutableIssue issue = issueManager.getIssueObject(docIssue.getId());
            issue.setResolution(resolution);
            issueManager.updateIssue(
                currentUser, issue, EventDispatchOption.ISSUE_UPDATED, sendEmail
            );
        }

        return "Completed for " + searchResult.getIssues().size() + " issues";
    }

    @Override
    public String getKey() {
        return "fixResolution";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.fixResolution";
    }

    @Override
    public boolean isHtml() {
        return false;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("jql", "Query", ParamType.STRING, false),
            new ScriptParamDto("resolution", "Resolution", ParamType.RESOLUTION, false),
            new ScriptParamDto("sendEmail", "Send email", ParamType.BOOLEAN, false)
        );
    }
}
