package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.util.velocity.NumberTool;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;

import javax.annotation.Nullable;
import java.util.Map;

public class NumberCFType extends ScriptedCFType<Number, Number> {
    protected NumberCFType(FieldConfigRepository configRepository, FieldValueExtractor valueExtractor) {
        super(configRepository, valueExtractor, Number.class);
    }

    @Override
    public void fillStaticVelocityParams(Map<String, Object> params) {
        params.put("numberTool", new NumberTool(this.getI18nBean().getLocale()));
    }

    @Override
    public void fillDynamicVelocityParams(Map<String, Object> params, Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {}

    @Override
    public JsonType getJsonSchema(CustomField customField) {
        return JsonTypeBuilder.custom("number", this.getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean b, @Nullable FieldLayoutItem fieldLayoutItem) {
        Double number = (Double)this.getValueFromIssue(field, issue);
        return new FieldJsonRepresentation(new JsonData(number));
    }
}
