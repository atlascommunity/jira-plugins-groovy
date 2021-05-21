package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import org.apache.lucene.document.Document;

public class DoubleCFExtractor implements LuceneFieldValueExtractor {
    private final String indexFieldName;
    private final DoubleConverter doubleConverter;

    public DoubleCFExtractor(String indexFieldName, DoubleConverter doubleConverter) {
        this.indexFieldName = indexFieldName;
        this.doubleConverter = doubleConverter;
    }

    @Override
    public Object extract(Document document) {
        String fieldValue = document.getField(indexFieldName).stringValue();
        return doubleConverter.getDouble(fieldValue);
    }
}
