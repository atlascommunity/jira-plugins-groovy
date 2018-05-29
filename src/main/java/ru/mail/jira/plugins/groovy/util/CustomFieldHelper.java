package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomFieldHelper {
    private final Logger logger = LoggerFactory.getLogger(CustomFieldHelper.class);
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

    public Class<Object> getExpectedType(CustomField field) {
        CustomFieldSearcher searcher = field.getCustomFieldSearcher();
        if (searcher != null) {
            String searcherKey = searcher.getDescriptor().getCompleteKey();
            Class searcherType = Const.SEARCHER_TYPES.get(searcherKey);
            if (searcherType != null) {
                return searcherType;
            } else {
                logger.warn("Unable to get type for searcher {}", searcherKey);
            }
        }

        return Object.class;
    }
}
