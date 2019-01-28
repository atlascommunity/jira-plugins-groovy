package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.*;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.CommentQueryParser;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.QueryParseResult;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

@Component
public class LastCommentFunction extends AbstractCommentQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(LastCommentFunction.class);
    private final QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator;
    private final IssueIdJoinQueryFactory issueIdJoinQueryFactory;
    private final SearchProviderFactory searchProviderFactory;
    private final SearchService searchService;
    private final SearchHelper searchHelper;

    @Autowired
    public LastCommentFunction(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        @ComponentImport SearchService searchService,
        CommentQueryParser commentQueryParser,
        QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator,
        @ComponentImport IssueIdJoinQueryFactory issueIdJoinQueryFactory,
        SearchHelper searchHelper
    ) {
        super(commentQueryParser, "lastComment", 1);
        this.searchProviderFactory = searchProviderFactory;
        this.searchService = searchService;
        this.queryPermissionDecorator = queryPermissionDecorator;
        this.issueIdJoinQueryFactory = issueIdJoinQueryFactory;
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

                String[] issueIds = issueIdCollector.getIssueIds().toArray(new String[0]);
                if (issueIds.length > 0) {
                    joinQuery = new FieldCacheTermsFilter(DocumentConstants.ISSUE_ID, issueIds);
                } else {
                    return QueryFactoryResult.createFalseResult();
                }
            }
        }

        logger.debug("constructed filter");

        LastCommentIdCollector lastCommentIdCollector = new LastCommentIdCollector();

        try {
            searcher.search(
                queryPermissionDecorator.createPermissionQuery(
                    queryCreationContext,
                    DocumentConstants.COMMENT_LEVEL, DocumentConstants.COMMENT_LEVEL_ROLE
                ),
                joinQuery, lastCommentIdCollector
            );
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        String[] commentIds = lastCommentIdCollector
            .lastCommentIds
            .values()
            .stream()
            .distinct()
            .map(String::valueOf)
            .toArray(String[]::new);

        logger.debug("collected last comments: {}", commentIds.length);

        if (commentIds.length == 0) {
            return QueryFactoryResult.createFalseResult();
        }

        QueryParseResult parseResult = parseParameters(queryCreationContext, args.get(withSubquery ? 1 : 0));

        if (parseResult.hasErrors()) {
            logger.error("Got errors while building query: {}", parseResult.getMessageSet());
            return QueryFactoryResult.createFalseResult();
        }

        logger.debug("constructed comment query");

        IssueIdCollector collector = new IssueIdCollector();

        try {
            searcher.search(
                parseResult.getQuery(),
                new FieldCacheTermsFilter(DocumentConstants.COMMENT_ID, commentIds),
                collector
            );
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        logger.debug("search complete");

        return new QueryFactoryResult(
            new ConstantScoreQuery(new IssueIdFilter(collector.getIssueIds())),
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

    private class LastCommentIdCollector extends Collector {
        private Map<String, String> lastCommentIds = new HashMap<>();
        private Map<String, String> lastDates = new HashMap<>();
        private String[] issueIds;
        private String[] commentIds;
        private String[] commentDates;

        @Override
        public void setScorer(Scorer scorer) {}

        @Override
        public void collect(int i) {
            String issue = issueIds[i];
            String commentId = commentIds[i];
            String date = commentDates[i];

            String lastDate = lastDates.get(issue);

            if (lastDate == null || date.compareTo(lastDate) >= 0) {
                lastCommentIds.put(issue, commentId);
                lastDates.put(issue, date);
            }
        }

        @Override
        public void setNextReader(IndexReader indexReader, int i) throws IOException {
            issueIds = FieldCache.DEFAULT.getStrings(indexReader, DocumentConstants.ISSUE_ID);
            commentIds = FieldCache.DEFAULT.getStrings(indexReader, DocumentConstants.COMMENT_ID);
            commentDates = FieldCache.DEFAULT.getStrings(indexReader, DocumentConstants.COMMENT_CREATED);
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }
    }
}
