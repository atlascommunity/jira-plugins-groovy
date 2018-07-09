package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import com.atlassian.jira.issue.Issue;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class TypeUtil {
    private static final Map<String, Class> FIELD_BASIC = ImmutableMap.of("issue", Issue.class);
    private static final Map<String, Class> FIELD_WITH_PARAMS = ImmutableMap.of(
        "issue", Issue.class,
        "velocityParams", Map.class
    );

    private TypeUtil() {}

    public static Map<String, Class> getFieldConfigTypes(boolean withVelocityParams) {
        if (withVelocityParams) {
            return FIELD_WITH_PARAMS;
        } else {
            return FIELD_BASIC;
        }
    }
}
