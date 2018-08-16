package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import lombok.Getter;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;
import ru.mail.jira.plugins.groovy.api.jql.ScriptFunction;

import javax.annotation.Nonnull;
import java.util.List;

public class ScriptFunctionAdapter implements CustomFunction {
    private final String key;
    private final String functionName;
    @Getter
    private final ScriptFunction delegate;

    public ScriptFunctionAdapter(String key, String functionName, ScriptFunction delegate) {
        this.key = key;
        this.functionName = functionName;
        this.delegate = delegate;
    }

    @Override
    public String getModuleKey() {
        return key;
    }

    @Nonnull
    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public void init(@Nonnull JqlFunctionModuleDescriptor jqlFunctionModuleDescriptor) {

    }

    @Nonnull
    @Override
    public JiraDataType getDataType() {
        return delegate.getDataType();
    }

    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return delegate.validate(applicationUser, functionOperand, terminalClause);
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return delegate.getValues(queryCreationContext, functionOperand, terminalClause);
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return delegate.getMinimumNumberOfExpectedArguments();
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        if (terminalClause.getOperand() instanceof FunctionOperand) {
            return new QueryFactoryResult(
                delegate.getQuery(queryCreationContext, ((FunctionOperand) terminalClause.getOperand())),
                terminalClause.getOperator() == Operator.NOT_IN
            );
        }
        return QueryFactoryResult.createFalseResult();
    }
}
