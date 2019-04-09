package ru.mail.jira.plugins.groovy.api.jql;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.List;

public interface ScriptedJqlValuesFunction extends ScriptedJqlFunction {
    List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, FunctionOperand functionOperand, TerminalClause terminalClause);
}
