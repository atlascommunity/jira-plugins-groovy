package ru.mail.jira.plugins.groovy.impl.jql.indexers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.indexers.impl.UserFieldIndexer;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.document.Document;
import org.ofbiz.core.entity.*;


@Scanned
public class LastUpdatedByIndexer extends UserFieldIndexer {
    public static final String LAST_UPDATED_BY_FIELD = "mrg_lastupd";

    private final OfBizDelegator ofBizDelegator;

    public LastUpdatedByIndexer(
        @ComponentImport FieldVisibilityManager fieldVisibilityManager,
        @ComponentImport OfBizDelegator ofBizDelegator
    ) {
        super(fieldVisibilityManager);
        this.ofBizDelegator = ofBizDelegator;
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
        OfBizListIterator changeGroups = this.ofBizDelegator.findListIteratorByCondition(
            "ChangeGroup",
            new EntityExpr("issue", EntityOperator.EQUALS, issue.getId()),
            null,
            null,
            ImmutableList.of("created DESC", "id DESC"),
            new EntityFindOptions().maxResults(1)
        );

        GenericValue changeGroup = changeGroups.next();
        if (changeGroup != null) {
            String author = changeGroup.getString("author");

            if (author != null) {
                this.indexUserKey(document, this.getDocumentFieldId(), author, issue);
            }
        }

        changeGroups.close();
    }
}
