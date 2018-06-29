package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Table("JQL_CHANGELOG")
public interface JqlFunctionScriptChangelog extends AbstractChangelog {
    @NotNull
    void setScript(JqlFunctionScript script);
    JqlFunctionScript getScript();
}
