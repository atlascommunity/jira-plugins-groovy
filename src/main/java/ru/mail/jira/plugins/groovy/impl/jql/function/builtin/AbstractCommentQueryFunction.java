package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.jql.query.QueryCreationContext;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.CommentQueryParser;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.QueryParseResult;

public abstract class AbstractCommentQueryFunction extends AbstractBuiltInFunction {
    private CommentQueryParser commentQueryParser;

    public AbstractCommentQueryFunction(
        CommentQueryParser commentQueryParser,
        String name, int minimumArgs
    ) {
        super(name, minimumArgs);

        this.commentQueryParser = commentQueryParser;
    }

    protected QueryParseResult parseParameters(
        QueryCreationContext queryCreationContext, String queryString
    ) {
        return commentQueryParser.parseParameters(queryCreationContext, queryString);
    }
}
