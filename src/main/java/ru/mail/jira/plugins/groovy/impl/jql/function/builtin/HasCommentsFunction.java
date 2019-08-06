package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.jql.query.QueryProjectRoleAndGroupPermissionsDecorator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdJoinQueryFactory;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class HasCommentsFunction extends AbstractBuiltInQueryFunction {
    private final IssueIdJoinQueryFactory issueIdJoinQueryFactory;
    private final QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator;

    @Autowired
    public HasCommentsFunction(
        IssueIdJoinQueryFactory issueIdJoinQueryFactory,
        QueryProjectRoleAndGroupPermissionsDecorator queryPermissionDecorator
    ) {
        super("hasComments", 0);
        this.issueIdJoinQueryFactory = issueIdJoinQueryFactory;
        this.queryPermissionDecorator = queryPermissionDecorator;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        return new QueryFactoryResult(
            issueIdJoinQueryFactory.createIssueIdJoinQuery(
                queryPermissionDecorator.createPermissionQuery(
                    queryCreationContext,
                    DocumentConstants.COMMENT_LEVEL, DocumentConstants.COMMENT_LEVEL_ROLE
                ),
                SearchProviderFactory.COMMENT_INDEX
            ),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
