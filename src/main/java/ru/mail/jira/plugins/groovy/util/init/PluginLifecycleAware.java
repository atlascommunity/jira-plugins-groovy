package ru.mail.jira.plugins.groovy.util.init;

public interface PluginLifecycleAware {
    void onStart();

    void onStop();

    int getInitOrder();
}
