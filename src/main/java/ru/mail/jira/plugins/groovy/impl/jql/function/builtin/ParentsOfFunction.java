package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParentsOfFunction extends AbstractSubTaskRelationFunction {
    @Autowired
    protected ParentsOfFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        @ComponentImport SubTaskManager subTaskManager,
        SearchHelper searchHelper
    ) {
        super(issueLinkTypeManager, subTaskManager, searchHelper, "parentsOf");
    }

    @Override
    protected Direction getDirection() {
        return Direction.IN;
    }
}
