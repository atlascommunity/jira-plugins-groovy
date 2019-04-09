package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.LastUpdatedQueryParser;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.QueryParseResult;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class LastUpdatedFunction extends AbstractBuiltInQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(LastUpdatedFunction.class);

    private final LastUpdatedQueryParser queryParser;

    @Autowired
    protected LastUpdatedFunction(
        LastUpdatedQueryParser queryParser
    ) {
        super("lastUpdated", 1);
        this.queryParser = queryParser;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        List<String> args = functionOperand.getArgs();

        String queryString = args.get(0);
        QueryParseResult parseResult = queryParser.parseParameters(new QueryCreationContextImpl(user), queryString);

        messageSet.addMessageSet(parseResult.getMessageSet());
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();

        String query = args.get(0);

        QueryParseResult parseResult = queryParser.parseParameters(queryCreationContext, query);

        if (parseResult.hasErrors()) {
            logger.error("Got errors while building query: {}", parseResult.getMessageSet());
            return QueryFactoryResult.createFalseResult();
        }

        return new QueryFactoryResult(
            parseResult.getQuery(),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
