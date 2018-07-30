package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class ScriptedCFType<T, S> extends CalculatedCFType<T, S> implements RestAwareCustomFieldType {
    private final FieldValueExtractor valueExtractor;

    private final Class<T> tType;

    protected ScriptedCFType(
        FieldValueExtractor valueExtractor,
        Class<T> tType
    ) {
        this.valueExtractor = valueExtractor;
        this.tType = tType;
    }

    @Override
    public String getStringFromSingularObject(S user) {
        return "";
    }

    @Override
    public S getSingularObjectFromString(String s) throws FieldValidationException {
        return null;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext) {
        return new FieldTypeInfo(null, null);
    }

    @Nullable
    @Override
    public T getValueFromIssue(CustomField field, Issue issue) {
        return valueExtractor.extractValue(field, issue, tType);
    }

    @Nonnull
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        return Lists.newArrayList(new ScriptedFieldConfigItemType());
    }

    @Nonnull
    @Override
    public final Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> parameters = super.getVelocityParameters(issue, field, fieldLayoutItem);
        fillStaticVelocityParams(parameters);
        fillDynamicVelocityParams(parameters, issue, field, fieldLayoutItem);
        return parameters;
    }

    public abstract void fillStaticVelocityParams(Map<String, Object> params);

    public abstract void fillDynamicVelocityParams(Map<String, Object> params, Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem);
}
