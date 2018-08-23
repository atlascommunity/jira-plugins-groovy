package ru.mail.jira.plugins.groovy.impl.jql;

import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemBuilder;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JqlInitializer {
    private final Logger logger = LoggerFactory.getLogger(JqlInitializer.class);

    private final CustomFieldManager customFieldManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    @Autowired
    public JqlInitializer(
        @ComponentImport CustomFieldManager customFieldManager,
        @ComponentImport ManagedConfigurationItemService managedConfigurationItemService
    ) {
        this.customFieldManager = customFieldManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
    }

    public void onStart() {
        boolean fieldExists = customFieldManager
            .getCustomFieldObjects()
            .stream()
            .anyMatch(it -> it.getCustomFieldType() instanceof JqlFunctionCFType);

        if (!fieldExists) {
            logger.warn("JqlFunctionCFType field doesn't exist, creating one");

            try {
                CustomField customField = customFieldManager.createCustomField(
                    "groovyFunction",
                    "Groovy jql function field type",
                    customFieldManager.getCustomFieldType("ru.mail.jira.plugins.groovy:groovy-jql-field"),
                    customFieldManager.getCustomFieldSearcher("ru.mail.jira.plugins.groovy:groovy-jql-searcher"),
                    ImmutableList.of(GlobalIssueContext.getInstance()),
                    FieldConfigSchemeManager.ALL_ISSUE_TYPES
                );

                ManagedConfigurationItem managedCustomField = managedConfigurationItemService.getManagedCustomField(customField);

                managedConfigurationItemService.updateManagedConfigurationItem(
                    ManagedConfigurationItemBuilder
                        .builder(managedCustomField)
                        .setManaged(true)
                        .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                        .build()
                );

                logger.info("created jql field");
            } catch (GenericEntityException e) {
                logger.error("unable to create custom field", e);
            }
        }
    }

    public void onStop() {}
}
