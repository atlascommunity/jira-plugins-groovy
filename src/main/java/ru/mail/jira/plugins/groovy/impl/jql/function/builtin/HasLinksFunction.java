package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class HasLinksFunction extends AbstractIssueLinkFunction {
    private final Logger logger = LoggerFactory.getLogger(HasLinksFunction.class);

    @Autowired
    public HasLinksFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        SearchHelper searchHelper
    ) {
        super(issueLinkTypeManager, searchHelper, "hasLinks", 0);
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();

        List<String> args = operand.getArgs();
        if (args.size() == 0) {
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

            for (IssueLinkType issueLinkType : issueLinkTypeManager.getIssueLinkTypes()) {
                booleanQuery.add(
                    new TermQuery(new Term(
                        DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(issueLinkType.getId())
                    )),
                    BooleanClause.Occur.SHOULD
                );
            }

            return new QueryFactoryResult(
                booleanQuery.build(),
                terminalClause.getOperator() == Operator.NOT_IN
            );
        } else if (args.size() == 1) {
            String linkName = args.get(0);
            Pair<IssueLinkType, LinkDirection> linkType = findLinkType(linkName);

            if (linkType != null) {
                LinkDirection linkDirection = linkType.right();
                Query query;

                if (linkDirection == LinkDirection.BOTH) {
                    BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

                    booleanQuery.add(
                        new TermQuery(new Term(
                            DocumentConstants.ISSUE_LINKS,
                            IssueLinkIndexer.createValue(linkType.left().getId(), Direction.IN)
                        )),
                        BooleanClause.Occur.SHOULD
                    );
                    booleanQuery.add(
                        new TermQuery(new Term(
                            DocumentConstants.ISSUE_LINKS,
                            IssueLinkIndexer.createValue(linkType.left().getId(), Direction.OUT)
                        )),
                        BooleanClause.Occur.SHOULD
                    );

                    query = booleanQuery.build();
                } else {
                    query = new TermQuery(new Term(
                        DocumentConstants.ISSUE_LINKS,
                        IssueLinkIndexer.createValue(linkType.left().getId(), linkDirection == LinkDirection.IN ? Direction.IN : Direction.OUT)
                    ));
                }

                return new QueryFactoryResult(
                    query,
                    terminalClause.getOperator() == Operator.NOT_IN
                );
            } else {
                logger.error("Link type wasn't found for \"{}\"", linkName);
            }
        }

        return QueryFactoryResult.createFalseResult();
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        if (functionOperand.getArgs().size() == 1) {
            String name = functionOperand.getArgs().get(0);

            Pair<IssueLinkType, LinkDirection> linkType = findLinkType(name);

            if (linkType == null) {
                messageSet.addErrorMessage("Unable to find link type with name \"" + name + "\"");
            }
        }
    }
}
