package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

public interface Listener extends Entity {
    @NotNull
    String getName();
    void setName(String name);

    @StringLength(StringLength.UNLIMITED)
    void setDescription(String description);
    String getDescription();

    @NotNull
    String getUuid();
    void setUuid(String uuid);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getScriptBody();
    void setScriptBody(String scriptBody);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getCondition();
    void setCondition(String condition);

    @OneToMany(reverse = "getListener")
    ListenerChangelog[] getChangelogs();

    @NotNull
    boolean isDeleted();
    void setDeleted(boolean deleted);
}
