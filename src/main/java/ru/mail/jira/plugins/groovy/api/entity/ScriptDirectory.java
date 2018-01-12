package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;

public interface ScriptDirectory extends Entity {
    @NotNull
    void setName(String name);
    String getName();

    void setParent(ScriptDirectory directory);
    ScriptDirectory getParent();

    @OneToMany(reverse = "getParent", where = "deleted = FALSE")
    ScriptDirectory[] getChildren();

    @OneToMany(reverse = "getDirectory", where = "deleted = FALSE")
    Script[] getScripts();

    @NotNull
    void setDeleted(boolean deleted);
    boolean isDeleted();
}
