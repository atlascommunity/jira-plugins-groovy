package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import io.atlassian.fugue.Either;
import org.apache.lucene.search.Query;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.CommentQueryParser;

public abstract class AbstractCommentQueryFunction extends AbstractBuiltInFunction {
    private CommentQueryParser commentQueryParser;

    public AbstractCommentQueryFunction(
        CommentQueryParser commentQueryParser,
        String name, int minimumArgs
    ) {
        super(name, minimumArgs);

        this.commentQueryParser = commentQueryParser;
    }

    protected Either<Query, MessageSet> parseParameters(
        QueryCreationContext queryCreationContext, String queryString
    ) {
        return commentQueryParser.parseParameters(queryCreationContext, queryString);
    }
}
