package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.SimpleCollector;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.apache.lucene.index.SortedSetDocValues.NO_MORE_ORDS;

public class LinkedIssueCollector extends SimpleCollector {
    private final Set<String> issueIds = new HashSet<>();
    private final Filter filter;
    private SortedSetDocValues docValues;

    public LinkedIssueCollector(Filter filter) {
        this.filter = filter;
    }

    public Set<String> getIssueIds() {
        return this.issueIds;
    }

    @Override
    protected void doSetNextReader(LeafReaderContext context) throws IOException {
        docValues = context.reader().getSortedSetDocValues(DocumentConstants.ISSUE_LINKS);
    }

    @Override
    public void collect(int doc) throws IOException {
        if (docValues.advanceExact(doc)) {
            long ord;

            while ((ord = docValues.nextOrd()) != NO_MORE_ORDS) {
                String value = docValues.lookupOrd(ord).utf8ToString();

                if (filter.test(value)) {
                    issueIds.add(value.substring(value.indexOf("i:") + 2));
                }
            }
        }
    }

    @Override
    public boolean needsScores() {
        return false;
    }

    public interface Filter {
        boolean test(String value);
    }
}
