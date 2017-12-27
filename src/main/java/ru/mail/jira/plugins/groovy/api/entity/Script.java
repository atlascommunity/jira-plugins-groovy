package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

//todo: add script type - condition/function/validator/listener etc
public interface Script extends Entity {
    @NotNull
    void setName(String name);
    String getName();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setScriptBody(String scriptBody);
    String getScriptBody();

    @NotNull
    void setDirectory(ScriptDirectory directory);
    ScriptDirectory getDirectory();

    @OneToMany(reverse = "getScript")
    Changelog[] getChangelogs();

    @NotNull
    void setDeleted(boolean deleted);
    boolean isDeleted();
}
