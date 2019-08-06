package ru.mail.jira.plugins.groovy.impl.jql.indexers;

import com.atlassian.jira.index.CommentSearchExtractor;
import com.atlassian.jira.issue.comments.Comment;
import com.google.common.collect.ImmutableSet;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class AdditionalFieldsCommentExtractor implements CommentSearchExtractor {
    public static final String COMMENT_ID_FIELD = "mygroovy.id";
    public static final String CREATED_FIELD = "mygroovy.created";

    private final Logger logger = LoggerFactory.getLogger(AdditionalFieldsCommentExtractor.class);

    @Override
    public Set<String> indexEntity(Context<Comment> context, Document document) {
        Comment comment = context.getEntity();

        logger.debug("indexing comment {}", comment.getId());

        BytesRef commentIdValue = new BytesRef(String.valueOf(comment.getId()));
        long createdValue = comment.getCreated().getTime();

        document.add(new StringField(COMMENT_ID_FIELD, commentIdValue, Field.Store.YES));
        document.add(new SortedDocValuesField(COMMENT_ID_FIELD, commentIdValue));
        document.add(new LongPoint(CREATED_FIELD, createdValue));
        document.add(new NumericDocValuesField(CREATED_FIELD, createdValue));

        return ImmutableSet.of(COMMENT_ID_FIELD, CREATED_FIELD);
    }
}
