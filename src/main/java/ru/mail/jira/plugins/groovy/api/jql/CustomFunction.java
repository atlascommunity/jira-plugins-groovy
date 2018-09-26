package ru.mail.jira.plugins.groovy.api.jql;

import com.atlassian.jira.plugin.jql.function.JqlFunction;

public interface CustomFunction extends JqlFunction {
    String getModuleKey();
}
