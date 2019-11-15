package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.jira.util.MessageSet;
import lombok.Getter;
import org.antlr.v4.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryBaseListener;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryLexer;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryParser;
import ru.mail.jira.plugins.groovy.util.AntlrUtil;

import java.util.HashMap;
import java.util.Map;

public class QueryUtil {
    private static final Logger logger = LoggerFactory.getLogger(QueryUtil.class);

    public static Map<String, String> parseQuery(MessageSet messageSet, String query) {
        CommentedQueryLexer lexer = new CommentedQueryLexer(CharStreams.fromString(query));
        TokenStream tokenStream = new CommonTokenStream(lexer);
        CommentedQueryParser parser = new CommentedQueryParser(tokenStream);

        QueryListener listener = new QueryListener();
        parser.addParseListener(listener);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e
            ) {
                messageSet.addErrorMessage("Parsing error at " + charPositionInLine + ": " + msg);
            }
        });

        parser.commented_query();

        if (logger.isDebugEnabled()) {
            logger.debug("Parsed values {}", listener.getValues());
        }

        return listener.getValues();
    }

    private static class QueryListener extends CommentedQueryBaseListener {
        @Getter
        private final Map<String, String> values = new HashMap<>();

        @Override
        public void exitBy_query(CommentedQueryParser.By_queryContext ctx) {
            values.put("by", AntlrUtil.unescapeString(ctx.username_expr().getText()));
        }

        @Override
        public void exitLike_query(CommentedQueryParser.Like_queryContext ctx) {
            values.put("like", AntlrUtil.unescapeString(ctx.str_expr().getText()));
        }

        @Override
        public void exitDate_query(CommentedQueryParser.Date_queryContext ctx) {
            values.put(ctx.date_field().getText(), AntlrUtil.unescapeString(ctx.date_expr().getText()));
        }

        @Override
        public void exitGroup_query(CommentedQueryParser.Group_queryContext ctx) {
            values.put(ctx.group_field().getText(), AntlrUtil.unescapeString(ctx.group_expr().getText()));
        }

        @Override
        public void exitRole_query(CommentedQueryParser.Role_queryContext ctx) {
            values.put(ctx.role_field().getText(), AntlrUtil.unescapeString(ctx.role_expr().getText()));
        }
    }
}
