package ru.mail.jira.plugins.groovy.impl.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypeImpl;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.search.Query;
import ru.mail.jira.plugins.groovy.impl.jql.JqlFunctionCFType;

import javax.annotation.Nonnull;
import java.util.List;

public interface ScriptFunction {
    @Nonnull
    default JiraDataType getDataType() {
        return new JiraDataTypeImpl(JqlFunctionCFType.class);
    }

    @Nonnull
    default MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return new MessageSetImpl();
    }

    @Nonnull
    default List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    default int getMinimumNumberOfExpectedArguments() {
        return 0;
    }

    Query getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand);
}
