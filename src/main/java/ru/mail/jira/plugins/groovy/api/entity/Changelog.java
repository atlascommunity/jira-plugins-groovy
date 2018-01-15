package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;

public interface Changelog extends AbstractChangelog {
    @NotNull
    void setScript(Script script);
    Script getScript();
}
