import com.atlassian.jira.JiraDataType
import com.atlassian.jira.JiraDataTypes
import com.atlassian.jira.jql.operand.QueryLiteral
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlValuesFunction

import javax.annotation.Nonnull

class TestJqlValuesFunction implements ScriptedJqlValuesFunction {
    @Override
    MessageSet validate(ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        MessageSet result = new MessageSetImpl()

        if (user.name == "kek") {

            result.addErrorMessage("user is kek")

            return result
        }

        return result
    }

    @Override
    List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, FunctionOperand functionOperand, TerminalClause terminalClause) {
        return [new QueryLiteral(functionOperand, queryCreationContext.applicationUser.key)]
    }

    @Override
    JiraDataType getDataType() {
        return JiraDataTypes.USER
    }
}
