package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;
import java.util.Set;

/**
 * based on atlassian IssueIdFilter, but with assumption that we can have multiple documents per issue_id
 * intended use case is to find comments that are related to these issue ids
 */
public class IssueIdMultipleEntryFilter extends Filter {
    private final Set<String> issuesIds;

    /**
     * @param issuesIds The list of issue ids to include in this filter
     */
    public IssueIdMultipleEntryFilter(final Set<String> issuesIds) {
        this.issuesIds = issuesIds;
    }

    @Override
    public DocIdSet getDocIdSet(final IndexReader indexReader) throws IOException {
        if (issuesIds.size() == 0) {
            return getEmptyDocIdSet();
        }

        return getOpenBitSet(indexReader);
    }

    private DocIdSet getEmptyDocIdSet() {
        return DocIdSet.EMPTY_DOCIDSET;
    }

    private OpenBitSet getOpenBitSet(final IndexReader reader) throws IOException {
        OpenBitSet bits = new OpenBitSet(reader.maxDoc());

        TermDocs termDocs = reader.termDocs();
        Term term = new Term(DocumentConstants.ISSUE_ID);

        for (String issueId : issuesIds) {
            term = term.createTerm(issueId);
            termDocs.seek(term);
            while (termDocs.next()) {
                bits.set(termDocs.doc());
            }
        }

        return bits;
    }
}
