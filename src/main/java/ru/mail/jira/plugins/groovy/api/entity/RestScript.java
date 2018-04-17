package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Unique;

public interface RestScript extends Entity {
    @NotNull
    @Unique
    void setName(String name);
    String getName();

    @NotNull
    void setUuid(String uuid);
    String getUuid();

    //comma separated http method names
    @NotNull
    void setMethods(String methods);
    String getMethods();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setScriptBody(String scriptBody);
    String getScriptBody();

    @StringLength(StringLength.UNLIMITED)
    void setGroups(String groups);
    String getGroups();

    @OneToMany(reverse = "getScript")
    RestChangelog[] getChangelogs();

    @NotNull
    void setDeleted(boolean deleted);
    boolean isDeleted();
}
