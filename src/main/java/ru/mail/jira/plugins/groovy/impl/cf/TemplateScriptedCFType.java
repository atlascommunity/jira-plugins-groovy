package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.util.velocity.NumberTool;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

//todo: preview
//todo: show template (&changelog) along with script
@Scanned
public class TemplateScriptedCFType extends ScriptedCFType<Object, Object> {
    private static final Map<String, Class> searcherTypes = ImmutableMap
        .<String, Class>builder()
        .put("com.atlassian.jira.plugin.system.customfieldtypes:exactnumber", Double.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:numberrange", Double.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher", String.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:exacttextsearcher", String.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:datetimerange", Date.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:daterange", Date.class)
        //todo: leave object for now, need to figure out how to provide several types
        .put("com.atlassian.jira.plugin.system.customfieldtypes:userpickergroupsearcher", Object.class)
        .build();

    private final Logger logger = LoggerFactory.getLogger(TemplateScriptedCFType.class);
    private final FieldValueExtractor valueExtractor;

    public TemplateScriptedCFType(
        FieldConfigRepository configRepository,
        FieldValueExtractor valueExtractor
    ) {
        super(configRepository, valueExtractor, Object.class);
        this.valueExtractor = valueExtractor;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        FieldScript script = valueExtractor.getScript(field, issue);

        Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        params.put("template", script != null ? script.getTemplate() : "");
        params.put("numberTool", new NumberTool(this.getI18nBean().getLocale()));

        if (script != null && script.isWithVelocityParams()) {
            params.putAll(valueExtractor.extractValueHolder(field, issue, getType(field)).getVelocityParams());
        }
        return params;
    }

    @Nullable
    @Override
    public Object getValueFromIssue(CustomField field, Issue issue) {
        return valueExtractor.extractValue(field, issue, getType(field));
    }

    private Class<Object> getType(CustomField field) {
        CustomFieldSearcher searcher = field.getCustomFieldSearcher();
        if (searcher != null) {
            String searcherKey = searcher.getDescriptor().getCompleteKey();
            Class searcherType = searcherTypes.get(searcherKey);
            if (searcherType != null) {
                return searcherType;
            } else {
                logger.warn("Unable to get type for searcher {}", searcherKey);
            }
        }

        return Object.class;
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
