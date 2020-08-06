package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.version;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.JqlFunctionParser;

@Component
public class ReleaseDateFunction extends AbstractVersionFunction {
    @Autowired
    protected ReleaseDateFunction(
        @ComponentImport VersionManager versionManager,
        @ComponentImport ProjectManager projectManager,
        @ComponentImport PermissionManager permissionManager,
        JqlFunctionParser jqlFunctionParser
    ) {
        super(versionManager, projectManager, permissionManager, jqlFunctionParser, Version::getReleaseDate, "releaseDate");
    }
}
