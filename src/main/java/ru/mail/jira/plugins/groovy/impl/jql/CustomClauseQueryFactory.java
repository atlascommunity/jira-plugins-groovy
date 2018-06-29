package ru.mail.jira.plugins.groovy.impl.jql;

import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;

import javax.annotation.Nonnull;

@Component
public class CustomClauseQueryFactory implements ClauseQueryFactory {
    private final Logger logger = LoggerFactory.getLogger(CustomClauseQueryFactory.class);

    private final ModuleManager moduleManager;

    @Autowired
    public CustomClauseQueryFactory(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        if (terminalClause.getOperand() instanceof FunctionOperand) {
            FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();

            CustomFunction function = moduleManager.getAllFunctions().get(operand.getName());

            if (function != null) {
                return function.getQuery(queryCreationContext, terminalClause);
            } else {
                logger.debug("function not found {}", operand.getName());
            }
        }

        return QueryFactoryResult.createFalseResult();
    }

}
