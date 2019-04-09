package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;

//https://ecosystem.atlassian.net/browse/AO-3454 FK column directory_id isn't preloaded by default, so we must specify all columns
@Preload({"ID", "NAME", "PARENT_ID", "DELETED"})
public interface ScriptDirectory extends Entity {
    @NotNull
    void setName(String name);
    String getName();

    void setParent(ScriptDirectory directory);
    ScriptDirectory getParent();

    @NotNull
    void setDeleted(boolean deleted);
    boolean isDeleted();
}
