package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlFunction;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class ScriptedFunctionAdapter<T extends ScriptedJqlFunction> implements CustomFunction {
    private final String key;
    private final String functionName;
    private final Supplier<T> delegateSupplier;
    private volatile T delegateInstance;

    protected ScriptedFunctionAdapter(String key, String functionName, Supplier<T> delegateSupplier) {
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
        return ClassLoaderUtil.runInContext(getDelegate()::getDataType);
    }

    @Nonnull
    @Override
    public final MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ClassLoaderUtil.runInContext(() -> getDelegate().validate(applicationUser, functionOperand, terminalClause));
    }

    @Override
    public final int getMinimumNumberOfExpectedArguments() {
        return ClassLoaderUtil.runInContext(getDelegate()::getMinimumNumberOfExpectedArguments);
    }

    public T getDelegate() {
        T result = delegateInstance;
        if (result == null) {
            synchronized (this) {
                result = delegateInstance;
                if (result == null) {
                    result = delegateSupplier.get();
                    delegateInstance = result;
                }
            }
        }
        return result;
    }

    public void reset() {
        T instance;
        synchronized (this) {
            instance = delegateInstance;
            delegateInstance = null;
        }

        if (instance != null) {
            InvokerHelper.removeClass(instance.getClass());
        }
    }
}
