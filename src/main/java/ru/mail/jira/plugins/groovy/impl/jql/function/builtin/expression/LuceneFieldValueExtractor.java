package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import org.apache.lucene.document.Document;

public interface LuceneFieldValueExtractor {
    Object extract(Document document);
}