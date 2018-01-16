package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;

public interface ListenerChangelog extends AbstractChangelog {
    @NotNull
    void setListener(Listener listener);
    Listener getListener();
}
