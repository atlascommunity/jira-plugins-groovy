package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.Polymorphic;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

@Polymorphic
public interface AbstractScript extends Entity {
    @NotNull
    void setName(String name);
    String getName();

    @StringLength(StringLength.UNLIMITED)
    void setDescription(String description);
    String getDescription();

    @NotNull
    boolean isDeleted();
    void setDeleted(boolean deleted);
}
