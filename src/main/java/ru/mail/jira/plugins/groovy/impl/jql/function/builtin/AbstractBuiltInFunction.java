package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypeImpl;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import ru.mail.jira.plugins.groovy.impl.jql.JqlFunctionCFType;
import ru.mail.jira.plugins.groovy.impl.jql.function.CustomFunction;

import javax.annotation.Nonnull;

public abstract class AbstractBuiltInFunction implements CustomFunction {
    private final String functionName;
    private final JiraDataType dataType;
    private final int minimumArgs;

    protected AbstractBuiltInFunction(String functionName, int minimumArgs) {
        this(functionName, new JiraDataTypeImpl(JqlFunctionCFType.class), minimumArgs);
    }

    protected AbstractBuiltInFunction(String functionName, JiraDataType dataType, int minimumArgs) {
        this.functionName = functionName;
        this.dataType = dataType;
        this.minimumArgs = minimumArgs;
    }

    @Override
    public final String getModuleKey() {
        return "jql-function-builtin-" + functionName;
    }

    @Nonnull
    @Override
    public final String getFunctionName() {
        return functionName;
    }

    @Override
    public final int getMinimumNumberOfExpectedArguments() {
        return minimumArgs;
    }

    @Override
    public final boolean isList() {
        return true;
    }

    @Nonnull
    @Override
    public final JiraDataType getDataType() {
        return dataType;
    }

    @Nonnull
    @Override
    public final MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        MessageSetImpl messageSet = new MessageSetImpl();

        validate(messageSet, applicationUser, functionOperand, terminalClause);

        return messageSet;
    }

    @Override
    public void init(@Nonnull JqlFunctionModuleDescriptor jqlFunctionModuleDescriptor) {}

    abstract protected void validate(
        MessageSet messageSet,
        ApplicationUser applicationUser,
        @Nonnull FunctionOperand functionOperand,
        @Nonnull TerminalClause terminalClause
    );
}
