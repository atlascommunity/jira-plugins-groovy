package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.jql.query.QueryProjectRoleAndGroupPermissionsDecorator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;
import ru.mail.jira.plugins.groovy.util.lucene.QueryUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

@Component
public class HasCommentsFunction extends AbstractBuiltInQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(HasCommentsFunction.class);
    private final SearchProviderFactory searchProviderFactory;
    private final QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator;

    @Autowired
    public HasCommentsFunction(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator
    ) {
        super("hasComments", 0);
        this.searchProviderFactory = searchProviderFactory;
        this.queryPermissionDecorator = queryPermissionDecorator;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        IssueIdCollector issueIdCollector = new IssueIdCollector();

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);

        try {
            searcher.search(
                queryPermissionDecorator.createPermissionQuery(
                    queryCreationContext,
                    DocumentConstants.COMMENT_LEVEL, DocumentConstants.COMMENT_LEVEL_ROLE
                ),
                issueIdCollector
            );
        } catch (IOException e) {
            logger.error("search error", e);
        }

        return new QueryFactoryResult(QueryUtil.createIssueIdQuery(issueIdCollector.getIssueIds()));
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
