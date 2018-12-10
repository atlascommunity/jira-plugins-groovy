package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.util.MessageSet;

public abstract class AbstractSubTaskFunction extends AbstractIssueLinkFunction {
    protected final SubTaskManager subTaskManager;

    protected AbstractSubTaskFunction(
        IssueLinkTypeManager issueLinkTypeManager,
        SubTaskManager subTaskManager,
        SearchHelper searchHelper,
        String functionName, int minimumArgs
    ) {
        super(issueLinkTypeManager, searchHelper, functionName, minimumArgs);
        this.subTaskManager = subTaskManager;
    }

    protected void validateSubTask(MessageSet messageSet) {
        if (!subTaskManager.isSubTasksEnabled()) {
            messageSet.addErrorMessage("SubTasks aren't enabled");
            return;
        }

        if (getSubTaskLinkType() == null) {
            messageSet.addErrorMessage("SubTask link type doesn't exist");
        }
    }

    protected IssueLinkType getSubTaskLinkType() {
        return issueLinkTypeManager
            .getIssueLinkTypesByStyle(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE)
            .stream()
            .findAny()
            .orElse(null);
    }
}
