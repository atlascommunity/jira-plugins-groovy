package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractSubTaskRelationFunction extends AbstractSubTaskFunction {
    protected AbstractSubTaskRelationFunction(
        IssueLinkTypeManager issueLinkTypeManager,
        SearchProvider searchProvider,
        SearchService searchService,
        SubTaskManager subTaskManager,
        String functionName
    ) {
        super(
            issueLinkTypeManager, searchProvider, searchService, subTaskManager, functionName, 1
        );
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        validateSubTask(messageSet);

        validateJql(messageSet, user, functionOperand.getArgs().get(0));
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = operand.getArgs();

        if (!subTaskManager.isSubTasksEnabled()) {
            logger.error("SubTasks are disabled");
            return QueryFactoryResult.createFalseResult();
        }

        Query jqlQuery = getQuery(user, args.get(0));

        if (jqlQuery == null) {
            return QueryFactoryResult.createFalseResult();
        }

        IssueLinkType subTaskLinkType = getSubTaskLinkType();

        if (subTaskLinkType == null) {
            logger.error("SubTask link type doesn't exist");
            return QueryFactoryResult.createFalseResult();
        }

        return getQuery(user, subTaskLinkType, getDirection(), jqlQuery);
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    abstract protected Direction getDirection();
}
