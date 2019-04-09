package ru.mail.jira.plugins.groovy.api.jql;

import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.query.operand.FunctionOperand;
import org.apache.lucene.search.Query;

import javax.annotation.Nonnull;

public interface ScriptedJqlQueryFunction extends ScriptedJqlFunction {
    Query getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand);
}
