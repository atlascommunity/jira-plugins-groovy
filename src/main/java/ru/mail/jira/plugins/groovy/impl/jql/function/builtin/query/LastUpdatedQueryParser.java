package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.indexers.LastUpdatedByIndexer;
import ru.mail.jira.plugins.groovy.util.compat.ArchivingHelper;

@Component
public class LastUpdatedQueryParser extends AbstractEntityQueryParser {
    @Autowired
    protected LastUpdatedQueryParser(
        @ComponentImport ProjectRoleManager projectRoleManager,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport ProjectManager projectManager,
        @ComponentImport JqlDateSupport jqlDateSupport,
        @ComponentImport GroupManager groupManager,
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
            null, LastUpdatedByIndexer.LAST_UPDATED_BY_FIELD, null, null, null
        );
    }

    @Override
    protected Query addPermissionsCheck(QueryCreationContext queryCreationContext, Query query) {
        return query;
    }
}
