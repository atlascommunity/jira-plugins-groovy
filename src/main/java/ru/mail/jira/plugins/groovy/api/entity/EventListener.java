package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

public interface EventListener extends Entity {
    @NotNull
    String getName();
    void setName(String name);

    String getUuid();
    void setUuid(String uuid);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getScript();
    void setScript(String script);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getCondition();
    void setCondition(String condition);

    @NotNull
    String getAuthorKey();
    void setAuthorKey(String authorKey);

    @NotNull
    boolean isDeleted();
    void setDeleted(boolean deleted);
}
