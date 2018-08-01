package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Collect Issue Ids for subquery searchers. Pretty much copy of Jira's IssueIdCollector
 *
 * @since v6.1
 */
public class IssueIdCollector extends Collector {
    private Set<String> issueIds;
    private String[] docIdToIssueId;

    public IssueIdCollector() {
        this.issueIds = new TreeSet<>();
    }

    @Override
    public void setScorer(final Scorer scorer) {
    }

    @Override
    public void collect(final int docId) {
        issueIds.add(docIdToIssueId[docId]);
    }

    @Override
    public void setNextReader(final IndexReader reader, final int docBase) throws IOException {
        docIdToIssueId = FieldCache.DEFAULT.getStrings(reader, DocumentConstants.ISSUE_ID);
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    public Set<String> getIssueIds() {
        return issueIds;
    }
}