package ru.mail.jira.plugins.groovy.util.cl;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.util.ClassLoaderStack;
import org.apache.felix.framework.BundleWiringImpl;
import org.osgi.framework.Bundle;

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

    public static String getClassBundleName(Class<?> type) {
        ClassLoader typeClassLoader = type.getClassLoader();

        if (typeClassLoader instanceof BundleWiringImpl.BundleClassLoader) {
            BundleWiringImpl.BundleClassLoader castedClassLoader = (BundleWiringImpl.BundleClassLoader) typeClassLoader;

            return ((Bundle) castedClassLoader.getBundle()).getSymbolicName();
        } else {
            throw new IllegalArgumentException("Class is not from osgi bundle");
        }
    }
}
