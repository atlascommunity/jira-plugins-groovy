package ru.mail.jira.plugins.groovy.impl.workflow.search;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

public interface WorkflowSearchCollector {
    void setWorkflow(JiraWorkflow workflow);
    void workflowComplete();
    void setAction(ActionDescriptor action);
    void actionComplete();
    void collect(ConditionDescriptor descriptor);
    void collect(ValidatorDescriptor descriptor, int order);
    void collect(FunctionDescriptor descriptor, int order);

    default boolean isAborted() {
        return false;
    }
}
