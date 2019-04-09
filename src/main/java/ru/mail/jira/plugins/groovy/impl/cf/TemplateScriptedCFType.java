package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.util.velocity.NumberTool;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScriptDto;
import ru.mail.jira.plugins.groovy.util.CustomFieldHelper;

import javax.annotation.Nullable;
import java.util.Map;

@Scanned
public class TemplateScriptedCFType extends ScriptedCFType<Object, Object> {
    private final Logger logger = LoggerFactory.getLogger(TemplateScriptedCFType.class);
    private final FieldValueExtractor valueExtractor;
    private final CustomFieldHelper customFieldHelper;

    public TemplateScriptedCFType(
        FieldValueExtractor valueExtractor,
        CustomFieldHelper customFieldHelper
    ) {
        super(valueExtractor, Object.class);
        this.valueExtractor = valueExtractor;
        this.customFieldHelper = customFieldHelper;
    }

    @Override
    public void fillStaticVelocityParams(Map<String, Object> params) {
        params.put("numberTool", new NumberTool(this.getI18nBean().getLocale()));
    }

    @Override
    public void fillDynamicVelocityParams(Map<String, Object> params, Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        FieldScriptDto script = valueExtractor.getScript(field, issue);

        params.put("template", script != null ? script.getTemplate() : "");

        if (script != null && script.isWithVelocityParams()) {
            params.putAll(valueExtractor.extractValueHolder(field, issue, customFieldHelper.getExpectedType(field)).getVelocityParams());
        }
    }

    @Nullable
    @Override
    public Object getValueFromIssue(CustomField field, Issue issue) {
        return valueExtractor.extractValue(field, issue, customFieldHelper.getExpectedType(field));
    }

    @Override
    public JsonType getJsonSchema(CustomField customField) {
        return JsonTypeBuilder.custom(JsonType.ANY_TYPE, this.getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField customField, Issue issue, boolean b, @Nullable FieldLayoutItem fieldLayoutItem) {
        return new FieldJsonRepresentation(new JsonData(null));
    }
}
