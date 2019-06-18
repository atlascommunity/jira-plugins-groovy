package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SimpleCollector;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Collect Issue Ids for subquery searchers. Pretty much copy of Jira's IssueIdCollector
 */
public class IssueIdCollector extends SimpleCollector {
    private Set<String> issueIds;
    private SortedDocValues docValues;

    public IssueIdCollector() {
        this.issueIds = new HashSet<>();
    }

    @Override
    public void setScorer(final Scorer scorer) {
    }

    @Override
    public void collect(final int docId) throws IOException {
        if (docValues.advanceExact(docId)) {
            issueIds.add(docValues.binaryValue().utf8ToString());
        }
    }

    public Set<String> getIssueIds() {
        return issueIds;
    }

    @Override
    protected void doSetNextReader(LeafReaderContext context) throws IOException {
        docValues = DocValues.getSorted(context.reader(), DocumentConstants.ISSUE_ID);
    }

    @Override
    public boolean needsScores() {
        return false;
    }
}
