package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.statistics.util.FieldDocumentHitCollector;
import com.google.common.collect.ImmutableSet;
import org.apache.lucene.document.Document;
import ru.mail.jira.plugins.groovy.impl.jql.indexers.LinksIndexer;

import java.util.HashSet;
import java.util.Set;

public class LinkedIssueCollector extends FieldDocumentHitCollector {
    private final Set<String> issueIds = new HashSet<>();
    private final Filter filter;

    public LinkedIssueCollector(Filter filter) {
        this.filter = filter;
    }

    @Override
    protected Set<String> getFieldsToLoad() {
        return ImmutableSet.of(LinksIndexer.LINKS_FIELD);
    }

    @Override
    public void collect(Document document) {
        for (String value : document.getValues(LinksIndexer.LINKS_FIELD)) {
            if (filter.test(value)) {
                issueIds.add(value.substring(value.indexOf("i:") + 2));
            }
        }
    }

    public Set<String> getIssueIds() {
        return this.issueIds;
    }

    public interface Filter {
        boolean test(String value);
    }
}
