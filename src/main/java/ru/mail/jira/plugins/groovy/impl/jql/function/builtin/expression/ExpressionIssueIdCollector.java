package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.statistics.util.FieldDocumentHitCollector;
import groovy.lang.Script;
import lombok.Getter;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExpressionIssueIdCollector extends FieldDocumentHitCollector {
    @Getter
    private final Set<String> issueIds = new HashSet<>();
    private final Map<String, LuceneFieldValueExtractor> extractorMap;
    private final Set<String> indexFieldsToLoad;
    private final Script script;


    public ExpressionIssueIdCollector(Set<String> fieldsToLoad, Script script, Map<String, LuceneFieldValueExtractor> extractorMap) {
        this.indexFieldsToLoad = fieldsToLoad;
        this.script = script;
        this.extractorMap = extractorMap;
    }

    @Override
    protected Set<String> getFieldsToLoad() {
        return this.indexFieldsToLoad;
    }

    @Override
    public void collect(Document document) {
        // add all document field values as properties
        extractorMap.keySet().forEach(indexFieldName -> {
            LuceneFieldValueExtractor extractor = extractorMap.get(indexFieldName);
            if (extractor != null)
                script.setProperty(indexFieldName, extractor.extract(document));
        });
        Object result = script.run();

        if (result.equals(Boolean.TRUE))
            issueIds.add(document.getField(DocumentConstants.ISSUE_ID).stringValue());
    }
}
