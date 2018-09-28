package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.GenericEntityException;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CustomFieldHelper {
    @ComponentImport
    @Inject
    private CustomFieldManager customFieldManager;

    public CustomField createNumberField() throws GenericEntityException {
        return createField(
            "ru.mail.jira.plugins.groovy:groovy-number-field",
            "com.atlassian.jira.plugin.system.customfieldtypes:exactnumber"
        );
    }

    public CustomField createField(String typeKey, String searcherKey) throws GenericEntityException {
        CustomFieldType type = customFieldManager.getCustomFieldType(typeKey);
        CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(searcherKey);

        return customFieldManager.createCustomField(
            "TEST_FIELD", "", type, searcher,
            ImmutableList.of(GlobalIssueContext.getInstance()),
            FieldConfigSchemeManager.ALL_ISSUE_TYPES
        );
    }

    public void deleteField(CustomField field) throws RemoveException {
        customFieldManager.removeCustomField(field);
    }

    public FieldConfig getFirstConfig(CustomField field) {
        return field.getConfigurationSchemes().iterator().next().getConfigs().values().iterator().next();
    }
}
