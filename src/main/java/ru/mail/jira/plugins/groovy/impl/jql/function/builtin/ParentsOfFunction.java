package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParentsOfFunction extends AbstractSubTaskRelationFunction {
    @Autowired
    protected ParentsOfFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService,
        @ComponentImport SubTaskManager subTaskManager
    ) {
        super(issueLinkTypeManager, searchProvider, searchService, subTaskManager, "parentsOf");
    }

    @Override
    protected Direction getDirection() {
        return Direction.IN;
    }
}
