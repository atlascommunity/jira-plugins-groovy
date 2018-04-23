package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;

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
