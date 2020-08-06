package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

import java.sql.Timestamp;

public interface ScriptExecution extends Entity {
    Script getScript();
    void setScript(Script script);

    @Indexed
    String getInlineId();
    void setInlineId(String inlineId);

    @NotNull
    Long getTime();
    void setTime(Long time);

    @NotNull
    Timestamp getDate();
    void setDate(Timestamp date);

    @Indexed
    @NotNull
    Boolean isSuccessful();
    void setSuccessful(Boolean successful);

    @StringLength(StringLength.UNLIMITED)
    String getError();
    void setError(String error);

    @StringLength(StringLength.UNLIMITED)
    String getLog();
    void setLog(String log);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getExtraParams();
    void setExtraParams(String extraParams);
}
