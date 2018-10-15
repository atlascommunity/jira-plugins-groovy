package ru.mail.jira.plugins.groovy.util.cl;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.util.ClassLoaderStack;

import java.util.function.Supplier;

public final class ClassLoaderUtil {
    private ClassLoaderUtil() {}

    public static ClassLoader getJiraClassLoader() {
        return JiraUtils.class.getClassLoader();
    }

    public static ClassLoader getCurrentPluginClassLoader() {
        return ClassLoaderUtil.class.getClassLoader();
    }

    public static  <T> T runInContext(Supplier<T> supplier) {
        ClassLoaderStack.push(getCurrentPluginClassLoader());
        try {
            return supplier.get();
        } finally {
            ClassLoaderStack.pop();
        }
    }
}
