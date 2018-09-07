package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentQueryParser extends AbstractEntityQueryParser {
    @Autowired
    public CommentQueryParser(
        @ComponentImport ProjectRoleManager projectRoleManager,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport ProjectManager projectManager,
        @ComponentImport JqlDateSupport jqlDateSupport,
        @ComponentImport GroupManager groupManager,
        @ComponentImport UserManager userManager
    ) {
        super(
            projectRoleManager,
            timeZoneManager,
            projectManager,
            jqlDateSupport,
            groupManager,
            userManager,
            DocumentConstants.COMMENT_CREATED, DocumentConstants.COMMENT_AUTHOR, DocumentConstants.COMMENT_BODY,
            DocumentConstants.COMMENT_LEVEL, DocumentConstants.COMMENT_LEVEL_ROLE
        );
    }
}
