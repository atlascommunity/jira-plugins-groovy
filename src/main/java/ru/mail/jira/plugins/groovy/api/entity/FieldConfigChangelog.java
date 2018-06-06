package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table("FIELD_CHANGELOG")
public interface FieldConfigChangelog extends AbstractChangelog {
    @NotNull
    void setFieldConfig(FieldConfig fieldConfig);
    FieldConfig getFieldConfig();

    @StringLength(StringLength.UNLIMITED)
    String getTemplateDiff();
    void setTemplateDiff();
}
