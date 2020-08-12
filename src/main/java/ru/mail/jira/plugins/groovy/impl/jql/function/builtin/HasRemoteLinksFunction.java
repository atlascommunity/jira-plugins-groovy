package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.indexers.RemoteLinksIndexer;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class HasRemoteLinksFunction extends AbstractBuiltInQueryFunction {
    @Autowired
    public HasRemoteLinksFunction() {
        super("hasRemoteLinks", 0);
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        if (functionOperand.getArgs().size() > 0)
            messageSet.addWarningMessage("Usage: hasRemoteLinks()");
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        Operand operand = terminalClause.getOperand();
        Operator operator = terminalClause.getOperator();
        if (operator.equals(Operator.IN)) {
            if (operand instanceof FunctionOperand) {
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                builder.add(new TermQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_HAS_ANY, "true")), BooleanClause.Occur.MUST);
                return new QueryFactoryResult(builder.build());
            }
        }
        return QueryFactoryResult.createFalseResult();
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
