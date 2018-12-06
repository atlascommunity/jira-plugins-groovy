package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import ru.mail.jira.plugins.groovy.api.jql.CustomQueryFunction;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlQueryFunction;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class QueryFunctionAdapter extends ScriptedFunctionAdapter<ScriptedJqlQueryFunction> implements CustomQueryFunction {
    public QueryFunctionAdapter(String key, String functionName, Supplier<ScriptedJqlQueryFunction> delegateSupplier) {
        super(key, functionName, delegateSupplier);
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        if (terminalClause.getOperand() instanceof FunctionOperand) {
            return new QueryFactoryResult(
                ClassLoaderUtil.runInContext(() ->
                    getDelegate().getQuery(queryCreationContext, ((FunctionOperand) terminalClause.getOperand()))
                ),
                terminalClause.getOperator() == Operator.NOT_IN
            );
        }
        return QueryFactoryResult.createFalseResult();
    }
}
