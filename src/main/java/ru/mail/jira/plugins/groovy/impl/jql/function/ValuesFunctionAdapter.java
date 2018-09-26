package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlValuesFunction;

import javax.annotation.Nonnull;
import java.util.List;

public class ValuesFunctionAdapter extends ScriptedFunctionAdapter<ScriptedJqlValuesFunction> {
    public ValuesFunctionAdapter(String key, String functionName, ScriptedJqlValuesFunction delegate) {
        super(key, functionName, delegate);
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return delegate.getValues(queryCreationContext, functionOperand, terminalClause);
    }
}
