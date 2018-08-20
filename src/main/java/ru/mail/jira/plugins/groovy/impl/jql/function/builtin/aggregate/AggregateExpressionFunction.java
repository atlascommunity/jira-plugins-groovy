package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.aggregate;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.AbstractBuiltInFunction;

import javax.annotation.Nonnull;
import java.util.List;

//todo need to figure out how to implement this without custom JavaScript @Component
public class AggregateExpressionFunction extends AbstractBuiltInFunction {
    //@Autowired
    public AggregateExpressionFunction() {
        super("myAggregateExpression", 2);
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        System.out.println("test");

        messageSet.addMessage(MessageSet.Level.WARNING, "test");
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        return new QueryFactoryResult(new MatchAllDocsQuery());
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
