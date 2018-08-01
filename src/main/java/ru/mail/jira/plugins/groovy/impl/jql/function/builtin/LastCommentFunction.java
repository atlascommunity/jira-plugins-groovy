package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.lucene.CommentIdFilter;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdMultipleEntryFilter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LastCommentFunction extends AbstractCommentQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(LastCommentFunction.class);
    private final SearchProviderFactory searchProviderFactory;
    private final SearchProvider searchProvider;
    private final SearchService searchService;

    @Autowired
    public LastCommentFunction(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        @ComponentImport ProjectRoleManager projectRoleManager,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport ProjectManager projectManager,
        @ComponentImport JqlDateSupport jqlDateSupport,
        @ComponentImport GroupManager groupManager,
        @ComponentImport UserManager userManager,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService
    ) {
        super(
            projectRoleManager, timeZoneManager, projectManager, jqlDateSupport, groupManager, userManager,
            "myLastComment", 1
        );
        this.searchProviderFactory = searchProviderFactory;
        this.searchProvider = searchProvider;
        this.searchService = searchService;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        //todo
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();

        boolean withSubquery = args.size() == 2;

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);

        logger.error("starting search");
        Query initialQuery;
        if (withSubquery) {
            String queryString = StringUtils.trimToEmpty(args.get(0));

            if (queryString.isEmpty()) {
                initialQuery = new MatchAllDocsQuery();
            } else {
                //if issue query is specified, get all issue ids that match jql query
                com.atlassian.query.Query issueQuery = getQuery(user, queryString, null);

                IssueIdCollector issueIdCollector = new IssueIdCollector();
                try {
                    searchProvider.search(issueQuery, user, issueIdCollector);
                } catch (SearchException e) {
                    logger.error("caught exception while searching", e);
                }

                Set<String> issueIds = issueIdCollector.getIssueIds();
                if (issueIds.size() > 0) {
                    initialQuery = new ConstantScoreQuery(new IssueIdMultipleEntryFilter(issueIds));
                } else {
                    return QueryFactoryResult.createFalseResult();
                }
            }
        } else {
            initialQuery = new MatchAllDocsQuery();
        }

        logger.error("constructed initial query");

        LastCommentIdCollector lastCommentIdCollector = new LastCommentIdCollector();

        try {
            searcher.search(initialQuery, lastCommentIdCollector);
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        Set<String> commentIds = lastCommentIdCollector
            .lastCommentIds
            .values()
            .stream()
            .map(String::valueOf)
            .collect(Collectors.toSet());

        logger.error("collected last comments: {}", commentIds.size());

        if (commentIds.size() == 0) {
            return QueryFactoryResult.createFalseResult();
        }

        Either<Query, MessageSet> parseResult = parseParameters(user, queryCreationContext.getDeterminedProjects(), args.get(withSubquery ? 1 : 0));

        if (parseResult.isRight()) {
            logger.error("Got errors while building query: {}", parseResult.right().get());
            return QueryFactoryResult.createFalseResult();
        }

        Query constructedQuery = parseResult.left().get();

        logger.error("constructed comment query");

        BooleanQuery query = new BooleanQuery();
        query.add(constructedQuery, BooleanClause.Occur.MUST);
        query.add(new ConstantScoreQuery(new CommentIdFilter(commentIds)), BooleanClause.Occur.MUST);

        IssueIdCollector collector = new IssueIdCollector();

        try {
            searcher.search(query, collector);
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        logger.error("search complete");

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
        private Map<String, Long> lastCommentIds = new HashMap<>();
        private String[] issueIds;
        private long[] commentIds;

        @Override
        public void setScorer(Scorer scorer) {
        }

        @Override
        public void collect(int i) {
            String issue = issueIds[i];
            long commentId = commentIds[i];

            lastCommentIds.merge(issue, commentId, this::mergeValue);
        }

        private Long mergeValue(Long a, Long b) {
            return Math.max(a, b);
        }

        @Override
        public void setNextReader(IndexReader indexReader, int i) throws IOException {
            issueIds = FieldCache.DEFAULT.getStrings(indexReader, DocumentConstants.ISSUE_ID);
            commentIds = FieldCache.DEFAULT.getLongs(indexReader, DocumentConstants.COMMENT_ID);
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }
    }
}
