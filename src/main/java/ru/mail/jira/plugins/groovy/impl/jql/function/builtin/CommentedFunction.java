package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Either;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

@Component
public class CommentedFunction extends AbstractCommentQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(CommentedFunction.class);

    private final SearchProviderFactory searchProviderFactory;

    @Autowired
    public CommentedFunction(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        @ComponentImport ProjectRoleManager projectRoleManager,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport ProjectManager projectManager,
        @ComponentImport JqlDateSupport jqlDateSupport,
        @ComponentImport GroupManager groupManager,
        @ComponentImport UserManager userManager
    ) {
        super(
            projectRoleManager, timeZoneManager, projectManager, jqlDateSupport, groupManager, userManager,
            "commented", 1
        );
        this.searchProviderFactory = searchProviderFactory;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        Either<Query, MessageSet> parseResult = parseParameters(user, ImmutableList.of(), functionOperand.getArgs().get(0));

        if (parseResult.isRight()) {
            messageSet.addMessageSet(parseResult.right().get());
        }
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);

        Either<Query, MessageSet> parseResult = parseParameters(user, queryCreationContext.getDeterminedProjects(), args.get(0));

        if (parseResult.isRight()) {
            logger.error("Got errors while building query: {}", parseResult.right().get());
            return QueryFactoryResult.createFalseResult();
        }

        IssueIdCollector collector = new IssueIdCollector();

        try {
            searcher.search(parseResult.left().get(), collector);
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        return new QueryFactoryResult(new ConstantScoreQuery(new IssueIdFilter(collector.getIssueIds())));
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
