package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.util.JiraUtils;

public final class ClassLoaderUtil {
    private ClassLoaderUtil() {}

    public static ClassLoader getJiraClassLoader() {
        return JiraUtils.class.getClassLoader();
    }
}
