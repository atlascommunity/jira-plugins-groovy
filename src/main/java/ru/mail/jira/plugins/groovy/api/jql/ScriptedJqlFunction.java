package ru.mail.jira.plugins.groovy.api.jql;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypeImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import ru.mail.jira.plugins.groovy.impl.jql.JqlFunctionCFType;

import javax.annotation.Nonnull;

public interface ScriptedJqlFunction {
    @Nonnull
    default JiraDataType getDataType() {
        return new JiraDataTypeImpl(JqlFunctionCFType.class);
    }

    @Nonnull
    default MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return new MessageSetImpl();
    }

    default int getMinimumNumberOfExpectedArguments() {
        return 0;
    }
}
