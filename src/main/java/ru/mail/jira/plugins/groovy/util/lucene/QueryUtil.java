package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Set;

public class QueryUtil {
    public static Query createIssueIdQuery(Set<String> issueIds) {
        return createMultiTermQuery(DocumentConstants.ISSUE_ID, issueIds);
    }

    public static Query createMultiTermQuery(String field, Set<String> values) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (String value : values) {
            builder.add(new TermQuery(new Term(field, value)), BooleanClause.Occur.SHOULD);
        }

        return new BooleanQuery.Builder().add(builder.build(), BooleanClause.Occur.FILTER).build();
    }
}
