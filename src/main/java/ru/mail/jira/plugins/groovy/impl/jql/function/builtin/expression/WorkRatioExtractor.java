package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import org.apache.lucene.document.Document;

public class WorkRatioExtractor implements LuceneFieldValueExtractor {
    private final String indexFieldName;

    public WorkRatioExtractor(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }
    @Override
    public Double extract(Document document) {
        String fieldValue = document.getField(indexFieldName).stringValue();
        return Double.parseDouble(fieldValue != null ? fieldValue : "0") / 100;
    }
}
