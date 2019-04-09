package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IssuesInEpics extends AbstractEpicFunction {
    @Autowired
    public IssuesInEpics(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        @ComponentImport PluginAccessor pluginAccessor,
        SearchHelper searchHelper
    ) {
        super(
            issueLinkTypeManager, pluginAccessor, searchHelper,
            "issuesInEpics", 1
        );
    }

    @Override
    protected Direction getDirection() {
        return Direction.OUT;
    }
}
