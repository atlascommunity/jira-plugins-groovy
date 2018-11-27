package ru.mail.jira.plugins.groovy.api.util;

public interface PluginLifecycleAware {
    void onStart();

    void onStop();

    int getInitOrder();
}
