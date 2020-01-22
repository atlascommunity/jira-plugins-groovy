import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import ru.mail.jira.scripts.go.MyGlobObj$TS$
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.Query
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlQueryFunction

import javax.annotation.Nonnull;

class TestJqlValuesFunction implements ScriptedJqlQueryFunction {
    @Override
    MessageSet validate(ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        def openStatus = MyGlobObj$TS$.myMethod();

        return new MessageSetImpl()
    }

    Query getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand) {
        def openStatus = MyGlobObj$TS$.myMethod();

        return new MatchAllDocsQuery()
    }
}
