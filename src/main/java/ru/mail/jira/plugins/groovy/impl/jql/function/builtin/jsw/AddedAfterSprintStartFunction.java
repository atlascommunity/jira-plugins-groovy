package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.jsw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.SearchHelper;
import ru.mail.jira.plugins.groovy.impl.jsw.JiraSoftwareHelper;
import ru.mail.jira.plugins.groovy.impl.jsw.JiraSoftwareHelperFactory;

@Component
public class AddedAfterSprintStartFunction extends AbstractSprintHistoryFunction {
    @Autowired
    public AddedAfterSprintStartFunction(
        JiraSoftwareHelperFactory jiraSoftwareHelperFactory,
        SearchHelper searchHelper
    ) {
        super(jiraSoftwareHelperFactory, searchHelper, "addedAfterSprintStart", true);
    }
}
