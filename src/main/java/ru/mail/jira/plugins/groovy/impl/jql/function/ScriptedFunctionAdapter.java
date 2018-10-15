package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import lombok.Getter;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlFunction;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import javax.annotation.Nonnull;

public abstract class ScriptedFunctionAdapter<T extends ScriptedJqlFunction> implements CustomFunction {
    private final String key;
    private final String functionName;
    @Getter
    protected final T delegate;

    protected ScriptedFunctionAdapter(String key, String functionName, T delegate) {
        this.key = key;
        this.functionName = functionName;
        this.delegate = delegate;
    }

    @Override
    public final String getModuleKey() {
        return key;
    }

    @Nonnull
    @Override
    public final String getFunctionName() {
        return functionName;
    }

    @Override
    public final boolean isList() {
        return true;
    }

    @Override
    public void init(@Nonnull JqlFunctionModuleDescriptor jqlFunctionModuleDescriptor) {

    }

    @Nonnull
    @Override
    public final JiraDataType getDataType() {
        return ClassLoaderUtil.runInContext(delegate::getDataType);
    }

    @Nonnull
    @Override
    public final MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ClassLoaderUtil.runInContext(() -> delegate.validate(applicationUser, functionOperand, terminalClause));
    }

    @Override
    public final int getMinimumNumberOfExpectedArguments() {
        return ClassLoaderUtil.runInContext(delegate::getMinimumNumberOfExpectedArguments);
    }
}
