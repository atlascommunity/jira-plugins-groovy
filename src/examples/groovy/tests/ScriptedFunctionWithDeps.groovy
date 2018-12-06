import com.atlassian.jira.JiraDataType
import com.atlassian.jira.JiraDataTypes
import com.atlassian.jira.issue.index.DocumentConstants
import com.atlassian.jira.issue.search.SearchProviderFactory
import com.atlassian.jira.issue.search.filters.IssueIdFilter
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import org.apache.lucene.index.Term
import org.apache.lucene.search.ConstantScoreQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlQueryFunction
import ru.mail.jira.plugins.groovy.api.script.StandardModule
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector

import javax.annotation.Nonnull

class TestJqlValuesFunction implements ScriptedJqlQueryFunction {
    private final SearchProviderFactory searchProviderFactory

    TestJqlValuesFunction(@StandardModule SearchProviderFactory searchProviderFactory) {
        this.searchProviderFactory = searchProviderFactory;
    }

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
    JiraDataType getDataType() {
        return JiraDataTypes.USER
    }

    Query getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand) {
        IssueIdCollector collector = new IssueIdCollector()
        searchProviderFactory
                .getSearcher(SearchProviderFactory.ISSUE_INDEX)
                .search(new TermQuery(new Term(DocumentConstants.ISSUE_ASSIGNEE, queryCreationContext.applicationUser.key)), collector)
        return new ConstantScoreQuery(new IssueIdFilter(collector.getIssueIds()))
    }
}
