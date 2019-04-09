package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
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
import io.atlassian.fugue.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LinkedIssuesOfFunction extends AbstractIssueLinkFunction {
    private final Logger logger = LoggerFactory.getLogger(LinkedIssuesOfFunction.class);

    @Autowired
    public LinkedIssuesOfFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        SearchHelper searchHelper
    ) {
        super(issueLinkTypeManager, searchHelper, "linkedIssuesOf", 1);
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        List<String> args = functionOperand.getArgs();

        searchHelper.validateJql(messageSet, applicationUser, args.get(0));

        if (args.size() == 2) {
            validateLinkType(messageSet, args.get(1));
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

        if (args.size() == 1) {
            BooleanQuery booleanQuery = new BooleanQuery();
            List<String> prefixes = issueLinkTypeManager
                .getIssueLinkTypes()
                .stream()
                .map(IssueLinkType::getId)
                .map(IssueLinkIndexer::createValue)
                .peek(it -> booleanQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_LINKS, it)), BooleanClause.Occur.SHOULD))
                .collect(Collectors.toList());

            LinkedIssueCollector collector = new LinkedIssueCollector(value -> {
                for (String prefix : prefixes) {
                    if (value.startsWith(prefix)) {
                        return true;
                    }
                }
                return false;
            });

            searchHelper.doSearch(jqlQuery, booleanQuery, collector, queryCreationContext);

            return new QueryFactoryResult(
                new ConstantScoreQuery(new IssueIdFilter(collector.getIssueIds())),
                terminalClause.getOperator() == Operator.NOT_IN
            );
        } else {
            String linkTypeName = args.get(1);
            Pair<IssueLinkType, LinkDirection> linkType = findLinkType(linkTypeName);

            if (linkType != null) {
                LinkDirection linkDirection = linkType.right();

                org.apache.lucene.search.Query result;
                if (linkDirection == LinkDirection.BOTH) {
                    BooleanQuery booleanQuery = new BooleanQuery();

                    booleanQuery.add(getQuery(linkType.left(), Direction.IN, jqlQuery, queryCreationContext), BooleanClause.Occur.SHOULD);
                    booleanQuery.add(getQuery(linkType.left(), Direction.OUT, jqlQuery, queryCreationContext), BooleanClause.Occur.SHOULD);

                    result = booleanQuery;
                } else {
                    result = getQuery(linkType.left(), linkDirection == LinkDirection.IN ? Direction.IN : Direction.OUT, jqlQuery, queryCreationContext);
                }

                return new QueryFactoryResult(result, terminalClause.getOperator() == Operator.NOT_IN);
            } else {
                logger.error("Link type \"{}\" wasn't found", linkTypeName);
            }
        }

        return QueryFactoryResult.createFalseResult();
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
