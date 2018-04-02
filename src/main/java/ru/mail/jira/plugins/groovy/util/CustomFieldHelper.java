package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomFieldHelper {
    private final FieldConfigManager fieldConfigManager;

    @Autowired
    public CustomFieldHelper(
        @ComponentImport FieldConfigManager fieldConfigManager
    ) {
        this.fieldConfigManager = fieldConfigManager;
    }

    public String getFieldName(Long fieldConfigId) {
        FieldConfig fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);

        if (fieldConfig != null) {
            return fieldConfig.getCustomField().getName();
        }

        return null;
    }
}
