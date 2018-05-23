package ru.mail.jira.plugins.groovy.impl.workflow.search;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.workflow.loader.*;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowActionDto;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowActionItem;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowDto;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.*;

public class ScriptUsageCollector implements WorkflowSearchCollector {
    private final int scriptId;
    private final List<WorkflowDto> result = new ArrayList<>();
    private List<WorkflowActionDto> currentActions = new ArrayList<>();
    private List<WorkflowActionItem> currentActionItems = new ArrayList<>();
    private JiraWorkflow workflow;
    private ActionDescriptor action;

    public ScriptUsageCollector(int scriptId) {
        this.scriptId = scriptId;
    }

    @Override
    public void setWorkflow(JiraWorkflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public void workflowComplete() {
        if (currentActions.size() > 0) {
            result.add(new WorkflowDto(
                workflow.getName(),
                workflow.isActive(),
                workflow.hasDraftWorkflow(),
                currentActions
            ));
            currentActions = new ArrayList<>();
        }
    }

    @Override
    public void setAction(ActionDescriptor action) {
        this.action = action;
    }

    @Override
    public void actionComplete() {
        if (currentActionItems.size() > 0) {
            Collection<StepDescriptor> steps = workflow.getStepsForTransition(action);

            currentActions.add(new WorkflowActionDto(
                action.getId(),
                steps.size() > 0 ? steps.iterator().next().getId() : null,
                action.getName(),
                currentActionItems
            ));
            currentActionItems = new ArrayList<>();
        }
    }

    @Override
    public void collect(ConditionDescriptor descriptor) {
        if (isUsed(WorkflowScriptType.CONDITION, descriptor.getArgs())) {
            currentActionItems.add(new WorkflowActionItem(WorkflowScriptType.CONDITION, null));
        }
    }

    @Override
    public void collect(ValidatorDescriptor descriptor, int order) {
        if (isUsed(WorkflowScriptType.VALIDATOR, descriptor.getArgs())) {
            currentActionItems.add(new WorkflowActionItem(WorkflowScriptType.VALIDATOR, order));
        }
    }

    @Override
    public void collect(FunctionDescriptor descriptor, int order) {
        if (isUsed(WorkflowScriptType.FUNCTION, descriptor.getArgs())) {
            currentActionItems.add(new WorkflowActionItem(WorkflowScriptType.FUNCTION, order));
        }
    }

    public List<WorkflowDto> getResult() {
        return result;
    }

    private boolean isUsed(WorkflowScriptType type, Map args) {
        String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);
        String itemScriptId = (String) args.get(Const.WF_REPOSITORY_SCRIPT_ID);

        return Objects.equals(moduleKey, type.getModuleKey()) && Objects.equals(itemScriptId, String.valueOf(scriptId));
    }
}
