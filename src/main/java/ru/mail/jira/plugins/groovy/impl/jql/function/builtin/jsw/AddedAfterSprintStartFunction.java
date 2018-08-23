package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.jsw;

import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jsw.JiraSoftwareHelper;

@Component
public class AddedAfterSprintStartFunction extends AbstractSprintHistoryFunction {
    @Autowired
    public AddedAfterSprintStartFunction(
        @ComponentImport SearchProvider searchProvider,
        JiraSoftwareHelper jiraSoftwareHelper
    ) {
        super(searchProvider, jiraSoftwareHelper, "addedAfterSprintStart", true);
    }
}
