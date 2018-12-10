package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.jsw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.SearchHelper;
import ru.mail.jira.plugins.groovy.impl.jsw.JiraSoftwareHelper;

@Component
public class AddedAfterSprintStartFunction extends AbstractSprintHistoryFunction {
    @Autowired
    public AddedAfterSprintStartFunction(
        JiraSoftwareHelper jiraSoftwareHelper,
        SearchHelper searchHelper
    ) {
        super(jiraSoftwareHelper, searchHelper, "addedAfterSprintStart", true);
    }
}
