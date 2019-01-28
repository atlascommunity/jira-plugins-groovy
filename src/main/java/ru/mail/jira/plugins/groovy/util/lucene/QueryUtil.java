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
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (String issueId : issueIds) {
            builder.add(new TermQuery(new Term(DocumentConstants.ISSUE_ID, issueId)), BooleanClause.Occur.SHOULD);
        }

        return new BooleanQuery.Builder().add(builder.build(), BooleanClause.Occur.FILTER).build();
    }
}
