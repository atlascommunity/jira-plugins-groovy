package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class ValuesFunctionAdapter extends ScriptedFunctionAdapter {
    public ValuesFunctionAdapter(String key, String functionName, Supplier<Object> delegateSupplier) {
        super(key, functionName, delegateSupplier);
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return (List<QueryLiteral>) ClassLoaderUtil.runInContext(() ->
            InvokerHelper.invokeMethod(getDelegate(), "getValues", new Object[] {queryCreationContext, functionOperand, terminalClause})
        );
    }
}
