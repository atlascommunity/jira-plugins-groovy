package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LinkedIssuesOfRecursive extends AbstractLinkedIssuesOfRecursiveFunction {
    @Autowired
    public LinkedIssuesOfRecursive(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        SearchHelper searchHelper
    ) {
        super(
            issueLinkTypeManager, searchHelper,
            "linkedIssuesOfRecursive", 1
        );
    }
}
