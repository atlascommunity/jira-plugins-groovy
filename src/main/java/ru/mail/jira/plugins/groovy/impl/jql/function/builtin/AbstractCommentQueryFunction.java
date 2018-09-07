package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import io.atlassian.fugue.Either;
import org.apache.lucene.search.Query;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.CommentQueryParser;

import java.util.Collection;

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
        ApplicationUser user, Collection<String> determinedProjects, String queryString
    ) {
        return commentQueryParser.parseParameters(user, determinedProjects, queryString);
    }
}
