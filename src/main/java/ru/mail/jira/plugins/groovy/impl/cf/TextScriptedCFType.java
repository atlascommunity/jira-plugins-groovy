package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;

import javax.annotation.Nullable;
import java.util.Map;

@Scanned
public class TextScriptedCFType extends ScriptedCFType<String, String> {
    protected TextScriptedCFType(
        FieldConfigRepository configRepository,
        FieldValueExtractor valueExtractor
    ) {
        super(configRepository, valueExtractor, String.class);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField) {
        return JsonTypeBuilder.custom(JsonType.STRING_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem) {
        String value = getValueFromIssue(field, issue);
        FieldJsonRepresentation bean = new FieldJsonRepresentation(new JsonData(value));

        if (field.isRenderable() && renderedVersionRequested && fieldLayoutItem != null) {
            final String content = ComponentAccessor.getComponent(RendererManager.class).getRenderedContent(fieldLayoutItem, issue);
            bean.setRenderedData(new JsonData(content));
        }

        return bean;
    }

    @Override
    public void fillStaticVelocityParams(Map<String, Object> params) {}

    @Override
    public void fillDynamicVelocityParams(Map<String, Object> params, Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {}
}
