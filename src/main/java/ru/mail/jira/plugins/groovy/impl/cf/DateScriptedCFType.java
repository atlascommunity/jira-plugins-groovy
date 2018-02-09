package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

@Scanned
public class DateScriptedCFType extends ScriptedCFType<Date, Date> implements DateField {
    private final DateFieldFormat dateFieldFormat;
    private final DateTimeFormatter iso8601Formatter;

    protected DateScriptedCFType(
        @ComponentImport DateFieldFormat dateFieldFormat,
        @ComponentImport DateTimeFormatterFactory dateTimeFormatterFactory,
        FieldConfigRepository configRepository,
        FieldValueExtractor valueExtractor
    ) {
        super(configRepository, valueExtractor, Date.class);
        this.dateFieldFormat = dateFieldFormat;
        this.iso8601Formatter = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE).withSystemZone();
    }

    @Override
    public JsonType getJsonSchema(CustomField customField) {
        return JsonTypeBuilder.custom(JsonType.DATE_TYPE, this.getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem) {
        Date date = this.getValueFromIssue(field, issue);
        if (date == null) {
            return new FieldJsonRepresentation(new JsonData(null));
        } else {
            FieldJsonRepresentation pair = new FieldJsonRepresentation(new JsonData(Dates.asDateString(date)));
            if (renderedVersionRequested) {
                pair.setRenderedData(new JsonData(dateFieldFormat.format(date)));
            }

            return pair;
        }
    }

    @Override
    public void fillStaticVelocityParams(Map<String, Object> params) {
        params.put("dateFieldFormat", this.dateFieldFormat);
        params.put("iso8601Formatter", this.iso8601Formatter);
    }

    @Override
    public void fillDynamicVelocityParams(Map<String, Object> params, Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {}
}
