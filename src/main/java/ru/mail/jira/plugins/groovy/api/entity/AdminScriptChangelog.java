package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Table("A_SCRIPT_CHANGELOG")
public interface AdminScriptChangelog extends AbstractChangelog {
    @NotNull
    void setScript(AdminScript script);
    AdminScript getScript();
}
