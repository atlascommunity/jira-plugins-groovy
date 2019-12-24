package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Unique;

import javax.annotation.Nonnull;

public interface GlobalObject extends AbstractScript {
    @NotNull
    @Unique
    void setLowerName(String lowerName);
    String getLowerName();

    @NotNull
    void setUuid(String uuid);
    String getUuid();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setScriptBody(String scriptBody);
    String getScriptBody();

    void setDependencies(String dependencies);
    String getDependencies();

    @OneToMany(reverse = "getScript")
    GlobalObjectChangelog[] getChangelogs();
}
