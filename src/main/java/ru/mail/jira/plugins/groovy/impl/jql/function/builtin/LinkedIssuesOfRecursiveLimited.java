package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LinkedIssuesOfRecursiveLimited extends AbstractLinkedIssuesOfRecursiveFunction {
    @Autowired
    public LinkedIssuesOfRecursiveLimited(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService,
        @ComponentImport SearchProviderFactory searchProviderFactory
    ) {
        super(
            issueLinkTypeManager, searchProvider, searchService, searchProviderFactory,
            "linkedIssuesOfRecursiveLimited", 2
        );
    }
}
