package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Unique;

public interface JqlFunctionScript extends AbstractScript {
    @NotNull
    @Unique
    void setName(String name);
    String getName();

    @NotNull
    void setUuid(String uuid);
    String getUuid();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setScriptBody(String scriptBody);
    String getScriptBody();

    @OneToMany(reverse = "getScript")
    JqlFunctionScriptChangelog[] getChangelogs();
}
