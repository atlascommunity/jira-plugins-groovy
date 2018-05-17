package ru.mail.jira.plugins.groovy.impl.workflow.search;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.workflow.loader.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WorkflowSearchService {
    private final WorkflowManager workflowManager;

    @Autowired
    public WorkflowSearchService(@ComponentImport WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public <T extends WorkflowSearchCollector> T search(T collector) {
        for (JiraWorkflow workflow : workflowManager.getWorkflows()) {
            collector.setWorkflow(workflow);
            for (ActionDescriptor action : workflow.getAllActions()) {
                collector.setAction(action);
                collectAction(action, collector);
                collector.actionComplete();
            }
            collector.workflowComplete();
        }
        return collector;
    }

    private void collectConditions(List input, WorkflowSearchCollector collector) {
        for (Object cond : input) {
            if (cond instanceof ConditionDescriptor) {
                collector.collect((ConditionDescriptor) cond);
            } else if (cond instanceof ConditionsDescriptor) {
                ConditionsDescriptor conditions = (ConditionsDescriptor) cond;
                collectConditions(conditions.getConditions(), collector);
            }
        }
    }

    private void collectAction(ActionDescriptor action, WorkflowSearchCollector collector) {
        RestrictionDescriptor restriction = action.getRestriction();
        if (restriction != null) {
            collectConditions(restriction.getConditionsDescriptor().getConditions(), collector);
        }

        int i = 1;
        for (Object val : action.getValidators()) {
            collector.collect((ValidatorDescriptor) val, i);

            ++i;
        }

        i = 1;
        ResultDescriptor unconditionalResult = action.getUnconditionalResult();
        if (unconditionalResult != null) {
            for (Object func : unconditionalResult.getPostFunctions()) {
                collector.collect((FunctionDescriptor) func, i);

                ++i;
            }
        }
    }
}
