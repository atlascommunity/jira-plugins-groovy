package ru.mail.jira.plugins.groovy.impl.jql.indexers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.List;

@Scanned
public class LastUpdatedByIndexer implements FieldIndexer {
    public static final String LAST_UPDATED_BY_FIELD = "mrg_lastupd";

    private final ChangeHistoryManager changeHistoryManager;

    public LastUpdatedByIndexer(
        @ComponentImport ChangeHistoryManager changeHistoryManager
    ) {
        this.changeHistoryManager = changeHistoryManager;
    }

    @Override
    public String getId() {
        return LAST_UPDATED_BY_FIELD;
    }

    @Override
    public String getDocumentFieldId() {
        return LAST_UPDATED_BY_FIELD;
    }

    @Override
    public boolean isFieldVisibleAndInScope(Issue issue) {
        return true;
    }

    @Override
    public void addIndex(Document document, Issue issue) {
        List<ChangeHistory> changeHistories = changeHistoryManager.getChangeHistories(issue);

        if (changeHistories.size() > 0) {
            ChangeHistory lastHistory = changeHistories.get(changeHistories.size() - 1);

            if (lastHistory.getAuthorKey() != null) {
                document.add(new Field(
                    LAST_UPDATED_BY_FIELD,
                    lastHistory.getAuthorKey(),
                    Field.Store.YES, Field.Index.NOT_ANALYZED
                ));
            }
        }
    }
}
