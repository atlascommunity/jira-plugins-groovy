package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;

public interface RestChangelog extends AbstractChangelog {
    @NotNull
    void setScript(RestScript script);
    RestScript getScript();
}
