package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.plugin.jql.function.JqlFunction;

public interface CustomFunction extends JqlFunction, ClauseQueryFactory {
    String getModuleKey();
}
