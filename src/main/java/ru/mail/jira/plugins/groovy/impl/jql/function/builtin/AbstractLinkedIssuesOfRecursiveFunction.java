package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
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
import ru.mail.jira.plugins.groovy.util.lucene.QueryUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractLinkedIssuesOfRecursiveFunction extends AbstractIssueLinkFunction {
    private final Logger logger = LoggerFactory.getLogger(AbstractLinkedIssuesOfRecursiveFunction.class);

    public AbstractLinkedIssuesOfRecursiveFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        SearchHelper searchHelper,
        String functionName, int minimumArgs
    ) {
        super(issueLinkTypeManager, searchHelper, functionName, minimumArgs);
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        List<String> args = functionOperand.getArgs();

        searchHelper.validateJql(messageSet, applicationUser, args.get(0));

        int argsOffset = 0;

        if (getMinimumNumberOfExpectedArguments() == 2) {
            argsOffset = 1;

            String maxIterationsValue = args.get(1);
            Integer maxIterations = Ints.tryParse(maxIterationsValue);

            if (maxIterations == null) {
                messageSet.addErrorMessage("Not a number - \"" + maxIterationsValue + "\"");
            } else if (maxIterations < 1) {
                messageSet.addErrorMessage("Invalid value 0 \"" + maxIterationsValue + "\"");
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

        Query jqlQuery = searchHelper.getQuery(user, args.get(0));

        if (jqlQuery == null) {
            return QueryFactoryResult.createFalseResult();
        }

        int argsOffset = 0;

        int maxIterations = 100;
        if (getMinimumNumberOfExpectedArguments() == 2) {
            argsOffset = 1;
            maxIterations = Ints.tryParse(args.get(1));
        }

        List<String> prefixes = null;
        BooleanQuery.Builder linkQueryBuilder = new BooleanQuery.Builder();

        if (args.size() == (1 + argsOffset)) {
            prefixes = issueLinkTypeManager
                .getIssueLinkTypes()
                .stream()
                .map(IssueLinkType::getId)
                .map(IssueLinkIndexer::createValue)
                .peek(it -> linkQueryBuilder.add(new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, it)), BooleanClause.Occur.SHOULD))
                .collect(Collectors.toList());
        } else {
            String linkTypeName = args.get(1 + argsOffset);
            Pair<IssueLinkType, LinkDirection> linkType = findLinkType(linkTypeName);

            if (linkType != null) {
                LinkDirection linkDirection = linkType.right();

                prefixes = new ArrayList<>();

                if (linkDirection == LinkDirection.BOTH) {
                    linkQueryBuilder.add(new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(linkType.left().getId(), Direction.IN))), BooleanClause.Occur.SHOULD);
                    linkQueryBuilder.add(new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(linkType.left().getId(), Direction.OUT))), BooleanClause.Occur.SHOULD);

                    prefixes.add(IssueLinkIndexer.createValue(linkType.left().getId()));
                } else {
                    Direction direction = linkDirection == LinkDirection.IN ? Direction.IN : Direction.OUT;

                    linkQueryBuilder.add(
                        new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(linkType.left().getId(), direction))),
                        BooleanClause.Occur.MUST
                    );

                    prefixes.add(IssueLinkIndexer.createValue(linkType.left().getId(), direction));
                }
            } else {
                logger.error("Link type \"{}\" wasn't found", linkTypeName);
            }
        }

        LinkedIssueCollector collector = createCollector(prefixes);

        BooleanQuery linkQuery = linkQueryBuilder.build();

        //initial iteration
        searchHelper.doSearch(jqlQuery, linkQuery, collector, queryCreationContext);

        Set<String> result = collector.getIssueIds();

        collectRecursiveLinks(linkQuery, prefixes, result, queryCreationContext, maxIterations);

        return new QueryFactoryResult(
            QueryUtil.createIssueIdQuery(result),
            terminalClause.getOperator() == Operator.NOT_IN
        );
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

    private void collectRecursiveLinks(org.apache.lucene.search.Query linksQuery, List<String> prefixes, Set<String> result, QueryCreationContext qcc, int maxIterations) {
        Set<String> nextIssueIds = result;

        int iteration = 1;
        while (true) {
            if (nextIssueIds.size() == 0 || iteration++ == maxIterations) {
                break;
            }

            LinkedIssueCollector iterationCollector = createCollector(prefixes);

            BooleanQuery.Builder query = new BooleanQuery.Builder();
            query.add(QueryUtil.createIssueIdQuery(nextIssueIds), BooleanClause.Occur.MUST);
            query.add(linksQuery, BooleanClause.Occur.MUST);

            searchHelper.doSearch(JqlQueryBuilder.newBuilder().buildQuery(), query.build(), iterationCollector, qcc);

            nextIssueIds = iterationCollector.getIssueIds();
            //remove all previously found issues to avoid infinite loop when there's cycled dependency
            nextIssueIds.removeAll(result);

            result.addAll(nextIssueIds);
        }
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
