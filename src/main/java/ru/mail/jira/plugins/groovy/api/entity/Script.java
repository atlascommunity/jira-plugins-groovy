package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

public interface Script extends Entity {
    @NotNull
    void setName(String name);
    String getName();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setScriptBody(String scriptBody);
    String getScriptBody();

    @StringLength(StringLength.UNLIMITED)
    void setParameters(String parameters);
    String getParameters();

    @NotNull
    void setDirectory(ScriptDirectory directory);
    ScriptDirectory getDirectory();

    @OneToMany(reverse = "getScript")
    Changelog[] getChangelogs();

    void setTypes(String types);
    String getTypes();

    @NotNull
    void setDeleted(boolean deleted);
    boolean isDeleted();
}
