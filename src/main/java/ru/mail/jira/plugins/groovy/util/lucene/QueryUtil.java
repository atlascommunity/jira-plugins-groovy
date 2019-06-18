package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryUtil {
    public static Query createIssueIdQuery(Set<String> issueIds) {
        return createIssueIdQuery(issueIds.stream().map(BytesRef::new).collect(Collectors.toList()));
    }

    public static Query createIssueIdQuery(Collection<BytesRef> issueIds) {
        return createMultiTermQuery(DocumentConstants.ISSUE_ID, issueIds);
    }

    public static Query createMultiTermQuery(String field, Collection<BytesRef> terms) {
        return new TermInSetQuery(field, terms);
    }
}
