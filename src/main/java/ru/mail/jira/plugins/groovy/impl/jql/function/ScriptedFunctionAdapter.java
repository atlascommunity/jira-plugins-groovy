package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class ScriptedFunctionAdapter implements CustomFunction {
    private final String key;
    private final String functionName;
    private final Supplier<Object> delegateSupplier;
    private volatile Object delegateInstance;

    protected ScriptedFunctionAdapter(
        String key,
        String functionName,
        Supplier<Object> delegateSupplier
    ) {
        this.key = key;
        this.functionName = functionName;
        this.delegateSupplier = delegateSupplier;
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
        return (JiraDataType) ClassLoaderUtil.runInContext(() ->
            InvokerHelper.invokeMethod(getDelegate(), "getDataType", null)
        );
    }

    @Nonnull
    @Override
    public final MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return (MessageSet) ClassLoaderUtil.runInContext(() ->
            InvokerHelper.invokeMethod(getDelegate(), "validate", new java.lang.Object[] {applicationUser, functionOperand, terminalClause})
        );
    }

    @Override
    public final int getMinimumNumberOfExpectedArguments() {
        return (int) ClassLoaderUtil.runInContext(() ->
            InvokerHelper.invokeMethod(getDelegate(), "getMinimumNumberOfExpectedArguments", null)
        );
    }

    public Object getDelegate() {
        Object proxy = delegateInstance;
        if (proxy == null) {
            synchronized (this) {
                proxy = delegateInstance;
                if (proxy == null) {
                    proxy = delegateSupplier.get();
                    delegateInstance = proxy;
                }
            }
        }
        return proxy;
    }

    public void reset() {
        Object instance;
        synchronized (this) {
            instance = delegateInstance;
            delegateInstance = null;
        }

        if (instance != null) {
            InvokerHelper.removeClass(instance.getClass());
        }
    }
}
