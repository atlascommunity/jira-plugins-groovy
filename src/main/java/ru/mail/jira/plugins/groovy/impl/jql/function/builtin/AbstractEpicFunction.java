package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractEpicFunction extends AbstractIssueLinkFunction {
    private final PluginAccessor pluginAccessor;

    protected AbstractEpicFunction(
        IssueLinkTypeManager issueLinkTypeManager,
        PluginAccessor pluginAccessor,
        SearchHelper searchHelper,
        String functionName, int minimumArgs
    ) {
        super(issueLinkTypeManager, searchHelper, functionName, minimumArgs);

        this.pluginAccessor = pluginAccessor;
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = operand.getArgs();

        if (!pluginAccessor.isPluginEnabled("com.pyxis.greenhopper.jira")) {
            return QueryFactoryResult.createFalseResult();
        }

        Query jqlQuery = searchHelper.getQuery(user, args.get(0));

        if (jqlQuery == null) {
            return QueryFactoryResult.createFalseResult();
        }

        IssueLinkType epicLinkType = getEpicLinkType();

        if (epicLinkType == null) {
            logger.error("Epic link type doesn't exist");
            return QueryFactoryResult.createFalseResult();
        }

        return new QueryFactoryResult(
            getQuery(epicLinkType, getDirection(), jqlQuery, queryCreationContext),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    private IssueLinkType getEpicLinkType() {
        if (pluginAccessor.isPluginEnabled("com.pyxis.greenhopper.jira")) {
            return issueLinkTypeManager
                .getIssueLinkTypesByStyle("jira_gh_epic_story")
                .stream()
                .findAny()
                .orElse(null);
        }

        return null;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        if (!pluginAccessor.isPluginEnabled("com.pyxis.greenhopper.jira")) {
            messageSet.addErrorMessage("Jira Software is not installed");
        }

        if (getEpicLinkType() == null) {
            messageSet.addErrorMessage("Epic link type doesn't exist");
        }

        searchHelper.validateJql(messageSet, user, functionOperand.getArgs().get(0));
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    abstract protected Direction getDirection();
}
