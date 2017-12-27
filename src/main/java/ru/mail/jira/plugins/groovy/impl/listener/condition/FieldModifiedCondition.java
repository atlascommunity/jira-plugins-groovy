package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.ofbiz.core.entity.GenericValue;

//todo: not sure if can be implemented properly
@Scanned
public class FieldModifiedCondition extends JiraEventCondition {
    private final ChangeHistoryManager changeHistoryManager;
    private final String field;

    public FieldModifiedCondition(
        @ComponentImport ChangeHistoryManager changeHistoryManager,
        String field
    ) {
        this.changeHistoryManager = changeHistoryManager;
        this.field = field;
    }

    @Override
    protected boolean passesCondition(IssueEvent issueEvent) {
        GenericValue changeLog = issueEvent.getChangeLog();
        if (changeLog != null) {
            ChangeHistory changeHistory = changeHistoryManager.getChangeHistoryById(changeLog.getLong("id"));

            if (changeHistory != null) {
                return changeHistory.getChangeItemBeans().stream().anyMatch(item -> field.equals(item.getField()));
            }
        }

        return false;
    }
}
