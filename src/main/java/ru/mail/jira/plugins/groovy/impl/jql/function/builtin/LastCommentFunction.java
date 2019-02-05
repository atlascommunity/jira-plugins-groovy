package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.statistics.util.FieldDocumentHitCollector;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.*;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.CommentQueryParser;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.QueryParseResult;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;
import ru.mail.jira.plugins.groovy.util.lucene.QueryUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

@Component
public class LastCommentFunction extends AbstractCommentQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(LastCommentFunction.class);
    private final QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator;
    private final SearchProviderFactory searchProviderFactory;
    private final SearchService searchService;
    private final SearchHelper searchHelper;

    @Autowired
    public LastCommentFunction(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        @ComponentImport SearchService searchService,
        CommentQueryParser commentQueryParser,
        QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator,
        SearchHelper searchHelper
    ) {
        super(commentQueryParser, "lastComment", 1);
        this.searchProviderFactory = searchProviderFactory;
        this.searchService = searchService;
        this.queryPermissionDecorator = queryPermissionDecorator;
        this.searchHelper = searchHelper;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        List<String> args = functionOperand.getArgs();

        String queryString = args.get(args.size() == 1 ? 0 : 1);
        QueryParseResult parseResult = parseParameters(new QueryCreationContextImpl(user), queryString);

        messageSet.addMessageSet(parseResult.getMessageSet());

        if (args.size() == 2) {
            SearchService.ParseResult jqlParseResult = searchService.parseQuery(user, args.get(0));

            if (!jqlParseResult.isValid()) {
                messageSet.addMessageSet(jqlParseResult.getErrors());
            }
        }
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();

        boolean withSubquery = args.size() == 2;

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);

        logger.debug("starting search");

        Query joinQuery = null;
        if (withSubquery) {
            String queryString = StringUtils.trimToEmpty(args.get(0));

            if (!queryString.isEmpty()) {
                //if issue query is specified, get all issue ids that match jql query
                com.atlassian.query.Query issueQuery = getQuery(user, queryString, null);

                IssueIdCollector issueIdCollector = new IssueIdCollector();

                searchHelper.doSearch(issueQuery, new MatchAllDocsQuery(), issueIdCollector, queryCreationContext);

                Set<String> issueIds = issueIdCollector.getIssueIds();
                if (issueIds.size() > 0) {
                    joinQuery = QueryUtil.createIssueIdQuery(issueIds);
                } else {
                    return QueryFactoryResult.createFalseResult();
                }
            }
        }

        logger.debug("constructed filter");

        LastCommentIdCollector lastCommentIdCollector = new LastCommentIdCollector();

        try {
            Query query;

            Query permissionQuery = queryPermissionDecorator.createPermissionQuery(
                queryCreationContext,
                DocumentConstants.COMMENT_LEVEL, DocumentConstants.COMMENT_LEVEL_ROLE
            );

            if (joinQuery != null) {
                query = new BooleanQuery.Builder()
                    .add(permissionQuery, BooleanClause.Occur.MUST)
                    .add(joinQuery, BooleanClause.Occur.MUST)
                    .build();
            } else {
                query = permissionQuery;
            }
            searcher.search(query, lastCommentIdCollector);
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        Set<String> commentIds = new HashSet<>(lastCommentIdCollector.lastCommentIds.values());

        logger.debug("collected last comments: {}", commentIds.size());

        if (commentIds.size() == 0) {
            return QueryFactoryResult.createFalseResult();
        }

        QueryParseResult parseResult = parseParameters(queryCreationContext, args.get(withSubquery ? 1 : 0));

        if (parseResult.hasErrors()) {
            logger.error("Got errors while building query: {}", parseResult.getMessageSet());
            return QueryFactoryResult.createFalseResult();
        }

        logger.debug("constructed comment query");

        BooleanQuery commentQuery = new BooleanQuery
            .Builder()
            .add(parseResult.getQuery(), BooleanClause.Occur.MUST)
            .add(QueryUtil.createMultiTermQuery(DocumentConstants.COMMENT_ID, commentIds), BooleanClause.Occur.MUST)
            .build();

        IssueIdCollector collector = new IssueIdCollector();

        try {
            searcher.search(
                commentQuery,
                collector
            );
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        logger.debug("search complete");

        return new QueryFactoryResult(
            QueryUtil.createIssueIdQuery(collector.getIssueIds()),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    private com.atlassian.query.Query getQuery(ApplicationUser user, String queryString, MessageSet messageSet) {
        SearchService.ParseResult queryResult = searchService.parseQuery(user, queryString);

        if (!queryResult.isValid()) {
            if (messageSet != null) {
                messageSet.addMessageSet(queryResult.getErrors());
            } else {
                logger.error("\"{}\" query is not valid {}", queryString, queryResult.getErrors());
            }

            return null;
        }

        return queryResult.getQuery();
    }

    private class LastCommentIdCollector extends FieldDocumentHitCollector {
        private Map<String, String> lastCommentIds = new HashMap<>();
        private Map<String, String> lastDates = new HashMap<>();

        @Override
        protected Set<String> getFieldsToLoad() {
            return ImmutableSet.of(DocumentConstants.ISSUE_ID, DocumentConstants.COMMENT_ID, DocumentConstants.COMMENT_CREATED);
        }

        @Override
        public void collect(Document document) {
            String issue = document.get(DocumentConstants.ISSUE_ID);
            String commentId = document.get(DocumentConstants.COMMENT_ID);
            String date = document.get(DocumentConstants.COMMENT_CREATED);

            String lastDate = lastDates.get(issue);

            if (lastDate == null || date.compareTo(lastDate) >= 0) {
                lastCommentIds.put(issue, commentId);
                lastDates.put(issue, date);
            }
        }
    }
}
