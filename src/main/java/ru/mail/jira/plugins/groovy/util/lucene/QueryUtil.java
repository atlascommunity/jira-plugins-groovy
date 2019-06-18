package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryUtil {
    private static Logger logger = LoggerFactory.getLogger(QueryUtil.class);

    @Deprecated
    public static Query createIssueIdQuery(Set<String> issueIds) {
        return createIssueIdQuery(issueIds.stream().map(BytesRef::new).collect(Collectors.toList()));
    }

    public static Query createIssueIdQuery(Collection<BytesRef> issueIds) {
        return createMultiTermQuery(DocumentConstants.ISSUE_ID, issueIds);
    }

    public static Query createMultiTermQuery(String field, Collection<BytesRef> terms) {
        //LoggerFactory.getLogger(QueryUtil.class).debug("Terms: {}; Null terms: {}", terms.size(), terms.stream().filter(Objects::isNull).count());
        logger.debug("constructing multi term query");
        TermInSetQuery termInSetQuery = new TermInSetQuery(field, terms);
        logger.debug("constructed multi term query");
        return termInSetQuery;
    }
}
