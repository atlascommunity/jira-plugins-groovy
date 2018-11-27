package ru.mail.jira.plugins.groovy.api;

import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;

import java.util.List;

public interface PluginLauncher {
    boolean isInitialized();

    List<PluginLifecycleAware> getLifecycleAwareObjects();
}
