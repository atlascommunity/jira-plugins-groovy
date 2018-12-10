package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.JiraDocValues;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.parameters.lucene.JiraBytesRef;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import ru.mail.jira.plugins.groovy.impl.jql.indexers.LinksIndexer;

import java.util.HashSet;
import java.util.Set;

public class LinkedIssueCollector extends Collector {
    private final Set<String> issueIds = new HashSet<>();
    private final Filter filter;

    private final ReaderCache readerCache = ComponentAccessor.getComponent(ReaderCache.class);
    private JiraDocValues docValues;

    public LinkedIssueCollector(Filter filter) {
        this.filter = filter;
    }

    @Override
    public void setScorer(Scorer scorer) {

    }

    @Override
    public void collect(int i) {
        JiraBytesRef[] values = this.docValues.getDocValues(i);

        for (JiraBytesRef byteValue : values) {
            String value = byteValue.utf8ToString();
            if (filter.test(value)) {
                issueIds.add(value.substring(value.indexOf("i:") + 2));
            }
        }
    }

    @Override
    public void setNextReader(IndexReader indexReader, int docBase) {
        docValues = readerCache.getDocValues(indexReader, LinksIndexer.LINKS_FIELD);
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    public Set<String> getIssueIds() {
        return this.issueIds;
    }

    public interface Filter {
        boolean test(String value);
    }
}
