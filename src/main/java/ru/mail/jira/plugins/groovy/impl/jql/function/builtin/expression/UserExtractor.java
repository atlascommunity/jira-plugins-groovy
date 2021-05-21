package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import org.apache.lucene.document.Document;

public class UserExtractor implements LuceneFieldValueExtractor {
    private final String indexFieldName;
    private final String emptyIndexValue;

    public UserExtractor(String indexFieldName, String emptyIndexValue) {
        this.indexFieldName = indexFieldName;
        this.emptyIndexValue = emptyIndexValue;
    }

    @Override
    public String extract(Document document) {
        String fieldValue = document.getField(indexFieldName).stringValue();
        if (fieldValue == null)
            return null;
        if (emptyIndexValue.equals(fieldValue))
            return null;

        return fieldValue;
    }
}