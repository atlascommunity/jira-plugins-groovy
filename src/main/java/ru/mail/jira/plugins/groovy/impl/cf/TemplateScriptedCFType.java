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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.util.Const;

import javax.annotation.Nullable;
import java.util.Map;

@Scanned
public class TemplateScriptedCFType extends ScriptedCFType<Object, Object> {
    private final Logger logger = LoggerFactory.getLogger(TemplateScriptedCFType.class);
    private final FieldValueExtractor valueExtractor;

    public TemplateScriptedCFType(
        FieldConfigRepository configRepository,
        FieldValueExtractor valueExtractor
    ) {
        super(configRepository, valueExtractor, Object.class);
        this.valueExtractor = valueExtractor;
    }

    @Override
    public void fillStaticVelocityParams(Map<String, Object> params) {
        params.put("numberTool", new NumberTool(this.getI18nBean().getLocale()));
    }

    @Override
    public void fillDynamicVelocityParams(Map<String, Object> params, Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        FieldScript script = valueExtractor.getScript(field, issue);

        params.put("template", script != null ? script.getTemplate() : "");

        if (script != null && script.isWithVelocityParams()) {
            params.putAll(valueExtractor.extractValueHolder(field, issue, getType(field)).getVelocityParams());
        }
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
            Class searcherType = Const.SEARCHER_TYPES.get(searcherKey);
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
