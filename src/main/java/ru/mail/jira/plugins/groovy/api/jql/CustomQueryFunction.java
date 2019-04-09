package ru.mail.jira.plugins.groovy.api.jql;

import com.atlassian.jira.jql.query.ClauseQueryFactory;

public interface CustomQueryFunction extends CustomFunction, ClauseQueryFactory {
    String getModuleKey();
}
