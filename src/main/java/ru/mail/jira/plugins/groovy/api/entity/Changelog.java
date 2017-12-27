package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

import java.sql.Timestamp;

public interface Changelog extends Entity {
    @NotNull
    void setAuthorKey(String authorKey);
    String getAuthorKey();

    @NotNull
    void setScript(Script script);
    Script getScript();

    @NotNull
    void setDate(Timestamp date);
    Timestamp getDate();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setDiff(String diff);
    String getDiff();

    @NotNull
    void setComment(String comment);
    String getComment();
}
