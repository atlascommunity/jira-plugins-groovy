package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.IssueIdJoinQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.*;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.CommentQueryParser;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.QueryParseResult;

import javax.annotation.Nonnull;
import java.util.*;

@Component
public class CommentedFunction extends AbstractCommentQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(CommentedFunction.class);

    private final IssueIdJoinQueryFactory issueIdJoinQueryFactory;

    @Autowired
    public CommentedFunction(
        IssueIdJoinQueryFactory issueIdJoinQueryFactory,
        CommentQueryParser commentQueryParser
    ) {
        super(commentQueryParser, "commented", 1);
        this.issueIdJoinQueryFactory = issueIdJoinQueryFactory;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        QueryParseResult parseResult = parseParameters(new QueryCreationContextImpl(user), functionOperand.getArgs().get(0));

        messageSet.addMessageSet(parseResult.getMessageSet());
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();

        QueryParseResult parseResult = parseParameters(queryCreationContext, args.get(0));

        if (parseResult.hasErrors()) {
            logger.error("Got errors while building query: {}", parseResult.getMessageSet());
            return QueryFactoryResult.createFalseResult();
        }

        return new QueryFactoryResult(
            issueIdJoinQueryFactory.createIssueIdJoinQuery(parseResult.getQuery(), SearchProviderFactory.COMMENT_INDEX),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(
        @Nonnull QueryCreationContext queryCreationContext,
        @Nonnull FunctionOperand functionOperand,
        @Nonnull TerminalClause terminalClause
    ) {
        return ImmutableList.of();
    }
}
