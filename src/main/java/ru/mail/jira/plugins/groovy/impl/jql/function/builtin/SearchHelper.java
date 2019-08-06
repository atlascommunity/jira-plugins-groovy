package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchQuery;
import com.atlassian.jira.jql.query.LuceneQueryBuilder;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchHelper {
    private final Logger logger = LoggerFactory.getLogger(SearchHelper.class);

    private final SearchProviderFactory searchProviderFactory;
    private final LuceneQueryBuilder luceneQueryBuilder;
    private final SearchProvider searchProvider;
    private final SearchService searchService;

    @Autowired
    public SearchHelper(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService
    ) {
        this.searchProviderFactory = searchProviderFactory;
        this.searchProvider = searchProvider;
        this.searchService = searchService;

        this.luceneQueryBuilder = ComponentAccessor.getComponent(LuceneQueryBuilder.class);
    }

    public void doSearch(Query jqlQuery, org.apache.lucene.search.Query luceneQuery, Collector collector, QueryCreationContext qcc) {
        try {
            if (qcc.isSecurityOverriden()) {
                BooleanQuery.Builder query = new BooleanQuery.Builder();

                if (jqlQuery != null && jqlQuery.getWhereClause() != null) {
                    query.add(luceneQueryBuilder.createLuceneQuery(qcc, jqlQuery.getWhereClause()), BooleanClause.Occur.MUST);
                }

                query.add(luceneQuery, BooleanClause.Occur.MUST);

                searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX).search(query.build(), collector);
            } else {
                searchProvider.search(SearchQuery.create(jqlQuery, qcc.getApplicationUser()).luceneQuery(luceneQuery), collector);
            }
        } catch (SearchException | IOException e) {
            logger.error("Caught exception while searching", e);
        }
    }

    public void validateJql(MessageSet messageSet, ApplicationUser user, String query) {
        SearchService.ParseResult parseResult = searchService.parseQuery(user, query);

        if (!parseResult.isValid()) {
            messageSet.addMessageSet(parseResult.getErrors());
        }
    }

    public Query getQuery(ApplicationUser user, String queryString) {
        SearchService.ParseResult queryResult = searchService.parseQuery(user, queryString);

        if (!queryResult.isValid()) {
            logger.error("\"{}\" query is not valid {}", queryString, queryResult.getErrors());

            return null;
        }

        return queryResult.getQuery();
    }
}
