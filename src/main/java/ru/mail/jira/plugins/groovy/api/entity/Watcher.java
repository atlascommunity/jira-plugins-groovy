package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.schema.Index;
import net.java.ao.schema.Indexes;
import net.java.ao.schema.NotNull;

@Indexes(
    @Index(name = "entity_type_id", methodNames = {"setEntityId", "setType"})
)
public interface Watcher extends Entity {
    @NotNull
    void setEntityId(Integer id);
    Integer getEntityId();

    @NotNull
    void setType(EntityType type);
    EntityType getType();

    @NotNull
    void setUserKey(String userKey);
    String getUserKey();
}
