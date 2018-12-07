package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import io.atlassian.fugue.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractLinkedIssuesOfRecursiveFunction extends AbstractIssueLinkFunction {
    private final Logger logger = LoggerFactory.getLogger(AbstractLinkedIssuesOfRecursiveFunction.class);

    private final SearchProviderFactory searchProviderFactory;

    public AbstractLinkedIssuesOfRecursiveFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService,
        @ComponentImport SearchProviderFactory searchProviderFactory,
        String functionName, int minimumArgs
    ) {
        super(issueLinkTypeManager, searchProvider, searchService, functionName, minimumArgs);
        this.searchProviderFactory = searchProviderFactory;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        List<String> args = functionOperand.getArgs();

        validateJql(messageSet, applicationUser, args.get(0));


        int argsOffset = 0;

        if (getMinimumNumberOfExpectedArguments() == 2) {
            argsOffset = 1;

            Integer maxIterations = Ints.tryParse(args.get(1));

            if (maxIterations == null) {
                messageSet.addErrorMessage("Not a number - \"" + args.get(1) + "\"");
            }
        }

        if (args.size() == (2 + argsOffset)) {
            validateLinkType(messageSet, args.get(1 + argsOffset));
        }
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = operand.getArgs();

        Query jqlQuery = getQuery(user, args.get(0));

        if (jqlQuery == null) {
            return QueryFactoryResult.createFalseResult();
        }

        int argsOffset = 0;

        int maxIterations = 100;
        if (getMinimumNumberOfExpectedArguments() == 2) {
            argsOffset = 1;
            maxIterations = Ints.tryParse(args.get(1));
        }

        if (args.size() == (1 + argsOffset)) {
            BooleanQuery booleanQuery = new BooleanQuery();
            List<String> prefixes = issueLinkTypeManager
                .getIssueLinkTypes()
                .stream()
                .map(IssueLinkType::getId)
                .map(IssueLinkIndexer::createValue)
                .peek(it -> booleanQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, it)), BooleanClause.Occur.SHOULD))
                .collect(Collectors.toList());

            LinkedIssueCollector collector = createCollector(prefixes);

            //initial iteration
            doSearch(jqlQuery, booleanQuery, collector, user);

            Set<String> result = collector.getIssueIds();

            collectRecursiveLinks(booleanQuery, prefixes, result, maxIterations);

            return new QueryFactoryResult(
                new ConstantScoreQuery(new IssueIdFilter(result)),
                terminalClause.getOperator() == Operator.NOT_IN
            );
        } else {
            String linkTypeName = args.get(1 + argsOffset);
            Pair<IssueLinkType, LinkDirection> linkType = findLinkType(linkTypeName);

            if (linkType != null) {
                LinkDirection linkDirection = linkType.right();

                List<String> prefixes = new ArrayList<>();

                org.apache.lucene.search.Query query;
                if (linkDirection == LinkDirection.BOTH) {
                    BooleanQuery booleanQuery = new BooleanQuery();

                    booleanQuery.add(getQuery(user, linkType.left(), Direction.IN, jqlQuery), BooleanClause.Occur.SHOULD);
                    booleanQuery.add(getQuery(user, linkType.left(), Direction.OUT, jqlQuery), BooleanClause.Occur.SHOULD);

                    prefixes.add(IssueLinkIndexer.createValue(linkType.left().getId()));

                    query = booleanQuery;
                } else {
                    Direction direction = linkDirection == LinkDirection.IN ? Direction.IN : Direction.OUT;

                    query = getQuery(user, linkType.left(), direction, jqlQuery);

                    prefixes.add(IssueLinkIndexer.createValue(linkType.left().getId(), direction));
                }

                IssueIdCollector issueIdCollector = new IssueIdCollector();

                IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);

                try {
                    searcher.search(query, issueIdCollector);
                } catch (IOException e) {
                    logger.warn("exception while search", e);
                }

                Set<String> result = issueIdCollector.getIssueIds();

                collectRecursiveLinks(new MatchAllDocsQuery(), prefixes, result, maxIterations);

                return new QueryFactoryResult(new ConstantScoreQuery(new IssueIdFilter(result)), terminalClause.getOperator() == Operator.NOT_IN);
            } else {
                logger.error("Link type \"{}\" wasn't found", linkTypeName);
            }
        }

        return QueryFactoryResult.createFalseResult();
    }

    private LinkedIssueCollector createCollector(List<String> prefixes) {
        return new LinkedIssueCollector(value -> {
            for (String prefix : prefixes) {
                if (value.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        });
    }

    private void collectRecursiveLinks(org.apache.lucene.search.Query query, List<String> prefixes, Set<String> result, int maxIterations) {
        Set<String> nextIssueIds = result;

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);

        int iteration = 1;
        while (true) {
            LinkedIssueCollector iterationCollector = createCollector(prefixes);
            try {
                searcher.search(query, new IssueIdFilter(nextIssueIds), iterationCollector);
            } catch (IOException e) {
                logger.warn("Exception while search", e);
            }

            nextIssueIds = iterationCollector.getIssueIds();
            //remove all previously found issues to avoid infinite loop when there's cycled dependency
            nextIssueIds.removeAll(result);

            result.addAll(nextIssueIds);
            if (nextIssueIds.size() == 0 || iteration++ == maxIterations) {
                break;
            }
        }
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
