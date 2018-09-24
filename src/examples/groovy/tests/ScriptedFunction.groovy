import com.atlassian.jira.issue.index.DocumentConstants
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import org.apache.lucene.index.Term
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import ru.mail.jira.plugins.groovy.api.jql.ScriptFunction

import javax.annotation.Nonnull

class TestJqlFunction implements ScriptFunction {
    MessageSet validate(ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        MessageSet result = new MessageSetImpl()

        if (user.name == "kek") {

            result.addErrorMessage("user is kek")

            return result
        }

        return result
    }

    Query getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand) {
        return new TermQuery(new Term(
            DocumentConstants.ISSUE_ASSIGNEE, queryCreationContext.applicationUser.key
        ));
    }
}
