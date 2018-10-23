package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Table("GO_CHANGELOG")
public interface GlobalObjectChangelog extends AbstractChangelog {
    @NotNull
    void setScript(GlobalObject script);
    GlobalObject getScript();
}
