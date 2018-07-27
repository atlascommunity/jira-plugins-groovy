package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class TypeUtil {
    private static final Map<String, Class> CONSOLE = ImmutableMap.of(
        "currentUser", ApplicationUser.class
    );
    private static final Map<String, Class> FIELD_BASIC = ImmutableMap.of(
        "issue", Issue.class
    );
    private static final Map<String, Class> FIELD_WITH_PARAMS = ImmutableMap.of(
        "issue", Issue.class,
        "velocityParams", Map.class
    );
    private static final Map<String, Class> WORKFLOW_GENERIC = ImmutableMap.of(
        "issue", MutableIssue.class,
        "currentUser", ApplicationUser.class,
        "transientVars", Map.class
    );

    private TypeUtil() {}

    public static Map<String, Class> getFieldConfigTypes(boolean withVelocityParams) {
        if (withVelocityParams) {
            return FIELD_WITH_PARAMS;
        } else {
            return FIELD_BASIC;
        }
    }

    public static Map<String, Class> getConsoleTypes() {
        return CONSOLE;
    }

    public static Map<String, Class> getWorkflowTypes() {
        return WORKFLOW_GENERIC;
    }
}
