package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import com.atlassian.jira.lucenelegacy.NumberTools;
import org.apache.lucene.document.Document;

public class DoubleExtractor implements LuceneFieldValueExtractor {
    private final String indexFieldName;
    public DoubleExtractor(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }
    @Override
    public Long extract(Document document) {
        String fieldValue = document.getField(indexFieldName).stringValue();
        return fieldValue != null ? NumberTools.stringToLong(fieldValue) : 0L;
    }
}