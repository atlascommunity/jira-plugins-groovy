package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.lucenelegacy.NumberTools;
import org.apache.lucene.document.Document;

public class MillisecondsExtractor implements LuceneFieldValueExtractor {
    private final String indexFieldName;

    public MillisecondsExtractor(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    @Override
    public Long extract(Document document) {
        String fieldValue = document.getField(indexFieldName).stringValue();
        if (fieldValue == null || FieldIndexer.NO_VALUE_INDEX_VALUE.equals(fieldValue)) {
            return 0L;
        } else {
            return NumberTools.stringToLong(fieldValue);
        }
    }
}