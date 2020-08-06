package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryProjectRoleAndGroupPermissionsDecorator;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.compat.ArchivingHelper;

@Component
public class CommentQueryParser extends AbstractEntityQueryParser {
    private final QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator;

    @Autowired
    public CommentQueryParser(
        @ComponentImport ProjectRoleManager projectRoleManager,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport ProjectManager projectManager,
        @ComponentImport JqlDateSupport jqlDateSupport,
        @ComponentImport GroupManager groupManager,
        QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator,
        JqlFunctionParser jqlFunctionParser,
        ArchivingHelper archivingHelper
    ) {
        super(
            projectRoleManager,
            timeZoneManager,
            projectManager,
            jqlDateSupport,
            groupManager,
            jqlFunctionParser,
            archivingHelper,
            false,
            DocumentConstants.COMMENT_CREATED, DocumentConstants.COMMENT_AUTHOR, DocumentConstants.COMMENT_BODY,
            DocumentConstants.COMMENT_LEVEL, DocumentConstants.COMMENT_LEVEL_ROLE
        );
        this.queryPermissionDecorator = queryPermissionDecorator;
    }

    @Override
    protected Query addPermissionsCheck(QueryCreationContext queryCreationContext, Query query) {
        return queryPermissionDecorator.appendPermissionFilterQuery(
            query, queryCreationContext, DocumentConstants.COMMENT_LEVEL, DocumentConstants.COMMENT_LEVEL_ROLE
        );
    }
}
