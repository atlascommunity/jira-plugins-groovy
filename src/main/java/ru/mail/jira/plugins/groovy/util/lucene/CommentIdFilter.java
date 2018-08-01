package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.SortedVIntList;

import java.io.IOException;
import java.util.Set;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.sort;

/*
  Based on atlassian IssueIdFilter (removed `reversed` parameter and looking for specific COMMENT_ID's instead if ISSUE_ID's)
 */
public class CommentIdFilter extends Filter {
    private final Set<String> commentIds;

    public CommentIdFilter(final Set<String> commentIds) {
        this.commentIds = commentIds;
    }

    @Override
    public DocIdSet getDocIdSet(final IndexReader indexReader) throws IOException {
        // We use different strategies here because the number of comments and change history items in the
        // indexes can be very larger, maybe 10s of millions of documents.

        if (commentIds.size() == 0) {
            return DocIdSet.EMPTY_DOCIDSET;
        }

        // For small numbers of issues we use the very compact and fast SortedVIntList
        // The SortedVIntList is smaller than the bit set if there are less than maxDoc / 8 issues
        // but it has the additional set up cost of requiring a sort.
        final boolean isSmallSet = commentIds.size() < indexReader.maxDoc() / 100;

        if (isSmallSet) {
            return getSortedVIntList(indexReader);
        }

        // Otherwise we use the larger but very fast bit set
        return getOpenBitSet(indexReader);
    }

    private DocIdSet getSortedVIntList(final IndexReader reader) throws IOException {
        int[] docIds = new int[commentIds.size()];
        int i = 0;
        TermDocs termDocs = reader.termDocs();
        for (String commentId : commentIds) {
            Term term = new Term(DocumentConstants.COMMENT_ID, commentId);
            termDocs.seek(term);
            // There is only one document per issue so just get it.
            // There may in fact be no match in this index segment.
            if (termDocs.next()) {
                docIds[i++] = termDocs.doc();
            }
        }
        // Because this is a segment reader only a few issues may be represented in each segment,
        // So the array might be only partially filled.
        int[] trimmedDocIds = copyOf(docIds, i);
        sort(trimmedDocIds);

        return new SortedVIntList(trimmedDocIds, i);
    }

    private OpenBitSet getOpenBitSet(final IndexReader reader) throws IOException {
        OpenBitSet bits = new OpenBitSet(reader.maxDoc());

        TermDocs termDocs = reader.termDocs();
        Term term = new Term(DocumentConstants.COMMENT_ID);

        // Seek through the term docs to see if we find each term
        for (String commentId : commentIds) {
            term = term.createTerm(commentId);
            termDocs.seek(term);
            // There is only one document per issue so just get it.
            // There may in fact be no match in this index segment.
            if (termDocs.next()) {
                bits.set(termDocs.doc());
            }
        }

        return bits;
    }
}
