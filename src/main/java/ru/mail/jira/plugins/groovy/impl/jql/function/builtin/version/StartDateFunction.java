package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.version;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.JqlFunctionParser;

@Component
public class StartDateFunction extends AbstractVersionFunction {
    @Autowired
    protected StartDateFunction(
        @ComponentImport VersionManager versionManager,
        @ComponentImport ProjectManager projectManager,
        JqlFunctionParser jqlFunctionParser
    ) {
        super(versionManager, projectManager, jqlFunctionParser, Version::getStartDate, "startDate");
    }
}
