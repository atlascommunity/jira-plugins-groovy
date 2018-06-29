package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
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
import com.google.common.collect.ImmutableList;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class HasLinkTypeFunction extends AbstractBuiltInFunction {
    private final Logger logger = LoggerFactory.getLogger(HasLinkTypeFunction.class);

    private final IssueLinkTypeManager issueLinkTypeManager;

    @Autowired
    public HasLinkTypeFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager
    ) {
        super("hasLinkType", 1);
        this.issueLinkTypeManager = issueLinkTypeManager;
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        String name = functionOperand.getArgs().get(0);

        if (findLinkType(name) == null) {
            messageSet.addErrorMessage("Unable to find link type with name \"" + name + "\"");
        }
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        if (operand.getArgs().size() == 1) {
            String name = operand.getArgs().get(0);
            IssueLinkType linkType = findLinkType(name);

            if (linkType != null) {
                return new QueryFactoryResult(new TermQuery(new Term(
                    DocumentConstants.ISSUE_LINKS,
                    IssueLinkIndexer.createValue(linkType.getId())
                )));
            } else {
                logger.error("Link type with name \"{}\" wasn't found", name);
            }
        }

        return QueryFactoryResult.createFalseResult();
    }

    private IssueLinkType findLinkType(String name) {
        for (IssueLinkType linkType : issueLinkTypeManager.getIssueLinkTypes()) {
            if (linkType.getName().equalsIgnoreCase(name)) {
                return linkType;
            }
        }
        return null;
    }
}
