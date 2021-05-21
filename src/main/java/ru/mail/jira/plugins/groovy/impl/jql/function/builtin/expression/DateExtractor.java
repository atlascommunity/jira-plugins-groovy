package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import org.apache.lucene.document.Document;

import java.sql.Timestamp;
import java.time.LocalDate;

public class DateExtractor implements LuceneFieldValueExtractor {
    private final String indexFieldName;

    public DateExtractor(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    @Override
    public Timestamp extract(Document document) {
        LocalDate localDate = LocalDate.ofEpochDay(document.getField(indexFieldName).numericValue().longValue());
        return Timestamp.valueOf(localDate.atStartOfDay());
    }
}