package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.jira.util.MessageSet;
import lombok.Getter;
import org.apache.lucene.search.Query;

@Getter
public class QueryParseResult {
    private final Query query;
    private final MessageSet messageSet;

    public QueryParseResult(Query query, MessageSet messageSet) {
        this.query = query;
        this.messageSet = messageSet;
    }

    public boolean hasErrors() {
        return messageSet.hasAnyErrors();
    }
}
