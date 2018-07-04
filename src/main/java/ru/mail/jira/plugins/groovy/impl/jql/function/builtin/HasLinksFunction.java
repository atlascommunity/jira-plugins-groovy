package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
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
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService
    ) {
        super(issueLinkTypeManager, searchProvider, searchService, "hasLinks", 0);
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        BooleanQuery booleanQuery = new BooleanQuery();

        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();

        List<String> args = operand.getArgs();
        if (args.size() == 0) {
            for (IssueLinkType issueLinkType : issueLinkTypeManager.getIssueLinkTypes()) {
                booleanQuery.add(
                    new TermQuery(new Term(
                        DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(issueLinkType.getId())
                    )),
                    BooleanClause.Occur.SHOULD
                );
            }

            return new QueryFactoryResult(booleanQuery);
        } else if (args.size() == 1) {
            String linkName = args.get(0);
            Pair<IssueLinkType, Direction> linkType = findLinkType(linkName);

            if (linkType != null) {
                return new QueryFactoryResult(new TermQuery(new Term(
                    DocumentConstants.ISSUE_LINKS, IssueLinkIndexer.createValue(linkType.left().getId(), linkType.right())
                )));
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

            Pair<IssueLinkType, Direction> linkType = findLinkType(name);

            if (linkType == null) {
                messageSet.addErrorMessage("Unable to find link type with name \"" + name + "\"");
            }
        }
    }
}
