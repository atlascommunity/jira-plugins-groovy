package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

@Table("FIELD_CONFIG")
public interface FieldScript extends Entity {
    @NotNull
    @Unique
    Long getFieldConfigId();
    void setFieldConfigId(Long fieldConfigId);

    @NotNull
    String getUuid();
    void setUuid(String uuid);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getScriptBody();
    void setScriptBody(String scriptBody);

    @StringLength(StringLength.UNLIMITED)
    String getTemplate();
    void setTemplate(String template);

    boolean isVelocityParamsEnabled();
    void setVelocityParamsEnabled(boolean velocityParamsEnabled);

    @NotNull
    Boolean getCacheable();
    void setCacheable(Boolean cacheable);

    @OneToMany(reverse = "getFieldConfig")
    FieldConfigChangelog[] getChangelogs();
}
