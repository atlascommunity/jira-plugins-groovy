package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.QueryParseResult;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.WorkLogQueryParser;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

@Component
public class WorkLoggedFunction extends AbstractBuiltInQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(WorkLoggedFunction.class);

    private final SearchProviderFactory searchProviderFactory;
    private final WorkLogQueryParser workLogQueryParser;

    @Autowired
    public WorkLoggedFunction(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        WorkLogQueryParser workLogQueryParser
    ) {
        super("workLogged", 1);
        this.searchProviderFactory = searchProviderFactory;
        this.workLogQueryParser = workLogQueryParser;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        QueryParseResult parseResult = workLogQueryParser.parseParameters(
            new QueryCreationContextImpl(user), functionOperand.getArgs().get(0)
        );

        messageSet.addMessageSet(parseResult.getMessageSet());
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.WORKLOG_INDEX);

        QueryParseResult parseResult = workLogQueryParser.parseParameters(queryCreationContext, args.get(0));

        if (parseResult.hasErrors()) {
            logger.error("Got errors while building query: {}", parseResult.getMessageSet());
            return QueryFactoryResult.createFalseResult();
        }

        IssueIdCollector collector = new IssueIdCollector();

        try {
            searcher.search(parseResult.getQuery(), collector);
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        return new QueryFactoryResult(
            new ConstantScoreQuery(new IssueIdFilter(collector.getIssueIds())),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(
        @Nonnull QueryCreationContext queryCreationContext,
        @Nonnull FunctionOperand functionOperand,
        @Nonnull TerminalClause terminalClause
    ) {
        return ImmutableList.of();
    }
}
