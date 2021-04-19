package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Collect Issue Ids for subquery searchers. Pretty much copy of Jira's IssueIdCollector
 */
public class IssueIdCollector extends SimpleCollector {
    private Set<BytesRef> issueIds;
    private SortedDocValues docValues;

    public IssueIdCollector() {
        this.issueIds = new TreeSet<>();
    }

    @Override
    public void setScorer(final Scorer scorer) {
    }

    @Override
    public void collect(final int docId) throws IOException {
        if (docValues.advanceExact(docId)) {
            issueIds.add(BytesRef.deepCopyOf(docValues.binaryValue()));
        }
    }

    public Set<BytesRef> getIssueIds() {
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
