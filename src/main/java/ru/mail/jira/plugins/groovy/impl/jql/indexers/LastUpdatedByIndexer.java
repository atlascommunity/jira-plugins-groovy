package ru.mail.jira.plugins.groovy.impl.jql.indexers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.index.indexers.impl.UserFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.lucene.document.Document;

import java.util.List;

@Scanned
public class LastUpdatedByIndexer extends UserFieldIndexer {
    public static final String LAST_UPDATED_BY_FIELD = "mrg_lastupd";

    private final ChangeHistoryManager changeHistoryManager;

    public LastUpdatedByIndexer(
        @ComponentImport ChangeHistoryManager changeHistoryManager,
        @ComponentImport FieldVisibilityManager fieldVisibilityManager
    ) {
        super(fieldVisibilityManager);
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
                this.indexUserKey(document, this.getDocumentFieldId(), lastHistory.getAuthorKey(), issue);// 22
            }
        }
    }
}
