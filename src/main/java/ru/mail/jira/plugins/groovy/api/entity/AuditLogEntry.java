package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;

import java.sql.Timestamp;

public interface AuditLogEntry extends Entity {
    @NotNull
    void setDate(Timestamp date);
    Timestamp getDate();

    @NotNull
    void setUserKey(String userKey);
    String getUserKey();

    @NotNull
    void setCategory(EntityType category);
    EntityType getCategory();

    void setEntityId(Integer entityId);
    Integer getEntityId();

    @NotNull
    void setAction(EntityAction action);
    EntityAction getAction();

    void setDescription(String description);
    String getDescription();
}
