package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import io.atlassian.fugue.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractIssueLinkFunction extends AbstractBuiltInFunction {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final IssueLinkTypeManager issueLinkTypeManager;
    protected final SearchProvider searchProvider;
    protected final SearchService searchService;

    @Autowired
    public AbstractIssueLinkFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService,
        String functionName, int minimumArgs
    ) {
        super(functionName, minimumArgs);
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.searchProvider = searchProvider;
        this.searchService = searchService;
    }

    protected QueryFactoryResult getQuery(ApplicationUser user, IssueLinkType linkType, Direction direction, Query jqlQuery) {
        TermQuery luceneQuery = new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(linkType.getId(), direction)));
        String expectedPrefix = IssueLinkIndexer.createValue(linkType.getId(), direction);
        LinkedIssueCollector collector = new LinkedIssueCollector(value -> value.startsWith(expectedPrefix));

        doSearch(jqlQuery, luceneQuery, collector, user);

        return new QueryFactoryResult(new ConstantScoreQuery(new IssueIdFilter(collector.getIssueIds())));
    }

    protected void doSearch(Query jqlQuery, org.apache.lucene.search.Query luceneQuery, Collector collector, ApplicationUser user) {
        try {
            searchProvider.search(jqlQuery, user, collector, luceneQuery);
        } catch (SearchException e) {
            logger.error("Caught exception while searching", e);
        }
    }

    protected void validateJql(MessageSet messageSet, ApplicationUser user, String query) {
        SearchService.ParseResult parseResult = searchService.parseQuery(user, query);

        if (!parseResult.isValid()) {
            messageSet.addMessageSet(parseResult.getErrors());
        }
    }

    protected void validateLinkType(MessageSet messageSet, String name) {
        Pair<IssueLinkType, Direction> linkType = findLinkType(name);

        if (linkType == null) {
            messageSet.addErrorMessage("Unable to find link type with name \"" + name + "\"");
        }
    }

    protected Pair<IssueLinkType, Direction> findLinkType(String name) {
        for (IssueLinkType linkType : issueLinkTypeManager.getIssueLinkTypes()) {
            if (linkType.getInward().equalsIgnoreCase(name)) {
                return Pair.pair(linkType, Direction.IN);
            }
            if (linkType.getOutward().equalsIgnoreCase(name)) {
                return Pair.pair(linkType, Direction.OUT);
            }
        }
        return null;
    }

    protected Query getQuery(ApplicationUser user, String queryString) {
        SearchService.ParseResult queryResult = searchService.parseQuery(user, queryString);

        if (!queryResult.isValid()) {
            logger.error("\"{}\" query is not valid {}", queryString, queryResult.getErrors());

            return null;
        }

        return queryResult.getQuery();
    }
}
