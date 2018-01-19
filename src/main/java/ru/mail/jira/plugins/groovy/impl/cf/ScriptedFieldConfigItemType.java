package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import ru.mail.jira.plugins.groovy.api.FieldConfigRepository;

public class ScriptedFieldConfigItemType implements FieldConfigItemType {
    private final FieldConfigRepository configRepository;

    public ScriptedFieldConfigItemType(FieldConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public String getDisplayName() {
        return "Groovy script calculated field";
    }

    @Override
    public String getDisplayNameKey() {
        return "ru.mail.jira.plugins.groovy.field.configItemTypeName";
    }

    @Override
    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem) {
        //todo: maybe render code?
        return "scripted field<br/>"; //todo    something like     webResourceManager.requireResource("jira.webresources:autocomplete");// 339
    }

    @Override
    public String getObjectKey() {
        return "mailru-groovy-script";
    }

    @Override
    public Object getConfigurationObject(Issue issue, FieldConfig fieldConfig) {
        return configRepository.getScript(fieldConfig.getId());
    }

    @Override
    public String getBaseEditUrl() {
        return "/plugins/servlet/my-groovy/custom-field";
    }
}
