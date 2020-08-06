package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.Query;
import io.atlassian.fugue.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.util.lucene.QueryUtil;

public abstract class AbstractIssueLinkFunction extends AbstractBuiltInQueryFunction {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final IssueLinkTypeManager issueLinkTypeManager;
    protected final SearchHelper searchHelper;

    public AbstractIssueLinkFunction(
        IssueLinkTypeManager issueLinkTypeManager,
        SearchHelper searchHelper,
        String functionName, int minimumArgs
    ) {
        super(functionName, minimumArgs);
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.searchHelper = searchHelper;
    }

    protected org.apache.lucene.search.Query getQuery(IssueLinkType linkType, Direction direction, Query jqlQuery, QueryCreationContext qcc) {
        TermQuery luceneQuery = new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(linkType.getId(), direction)));
        String expectedPrefix = IssueLinkIndexer.createValue(linkType.getId(), direction);
        LinkedIssueCollector collector = new LinkedIssueCollector(value -> value.startsWith(expectedPrefix));

        searchHelper.doSearch(jqlQuery, luceneQuery, collector, qcc);

        return QueryUtil.createIssueIdQuery(collector.getIssueIds());
    }

    protected void validateLinkType(MessageSet messageSet, String name) {
        Pair<IssueLinkType, LinkDirection> linkType = findLinkType(name);

        if (linkType == null) {
            messageSet.addErrorMessage("Unable to find link type with name \"" + name + "\"");
        }
    }

    protected Pair<IssueLinkType, LinkDirection> findLinkType(String name) {
        for (IssueLinkType linkType : issueLinkTypeManager.getIssueLinkTypes(false)) {
            boolean inward = linkType.getInward().equalsIgnoreCase(name);
            boolean outward = linkType.getOutward().equalsIgnoreCase(name);
            if (inward && outward) {
                return Pair.pair(linkType, LinkDirection.BOTH);
            }
            if (inward) {
                return Pair.pair(linkType, LinkDirection.IN);
            }
            if (outward) {
                return Pair.pair(linkType, LinkDirection.OUT);
            }
        }
        return null;
    }

    public enum LinkDirection {
        IN, OUT, BOTH
    }
}
