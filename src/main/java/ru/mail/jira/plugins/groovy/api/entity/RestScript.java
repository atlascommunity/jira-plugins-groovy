package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

public interface RestScript extends Entity {
    @NotNull
    void setName(String name);
    String getName();

    //comma separated http method names
    void setMethods(String methods);
    String getMethods();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setScriptBody(String scriptBody);
    String getScriptBody();

    @OneToMany(reverse = "getScript")
    RestChangelog[] getChangelogs();

    @NotNull
    void setDeleted(boolean deleted);
    boolean isDeleted();
}
