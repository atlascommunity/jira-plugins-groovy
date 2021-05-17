package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import org.apache.lucene.document.Document;

import java.sql.Timestamp;

public class DateTimeExtractor implements LuceneFieldValueExtractor {
    private final String indexFieldName;

    public DateTimeExtractor(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    @Override
    public Timestamp apply(Document document) {
        return new Timestamp(document.getField(indexFieldName).numericValue().longValue());
    }
}
