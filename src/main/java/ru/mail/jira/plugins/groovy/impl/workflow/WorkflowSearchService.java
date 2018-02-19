package ru.mail.jira.plugins.groovy.impl.workflow;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.workflow.loader.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowActionDto;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowActionItem;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowDto;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WorkflowSearchService {
    private final WorkflowManager workflowManager;

    @Autowired
    public WorkflowSearchService(
        @ComponentImport WorkflowManager workflowManager
    ) {
        this.workflowManager = workflowManager;
    }

    public List<WorkflowDto> findScriptUsages(int scriptId) {
        return workflowManager
            .getWorkflows()
            .stream()
            .flatMap(workflow -> getWorkflow(workflow, scriptId))
            .collect(Collectors.toList());
    }

    private Stream<WorkflowDto> getWorkflow(JiraWorkflow workflow, int scriptId) {
        List<WorkflowActionDto> actions = workflow
            .getAllActions()
            .stream()
            .flatMap(action -> getAction(action, scriptId))
            .collect(Collectors.toList());

        if (actions.size() > 0) {
            return Stream.of(new WorkflowDto(workflow.getName(), workflow.isActive(), workflow.hasDraftWorkflow(), actions));
        }

        return Stream.of();
    }

    private void collectConditions(List input, int scriptId, List<WorkflowActionItem> result) {
        for (Object cond : input) {
            if (cond instanceof ConditionDescriptor) {
                ConditionDescriptor condition = (ConditionDescriptor) cond;

                Map args = condition.getArgs();

                if (isUsed(WorkflowScriptType.CONDITION, args, scriptId)) {
                    result.add(new WorkflowActionItem(WorkflowScriptType.CONDITION, null));
                }
            } else if (cond instanceof ConditionsDescriptor) {
                ConditionsDescriptor conditions = (ConditionsDescriptor) cond;
                collectConditions(conditions.getConditions(), scriptId, result);
            }
        }
    }

    private Stream<WorkflowActionDto> getAction(ActionDescriptor action, int scriptId) {
        List<WorkflowActionItem> items = new ArrayList<>();

        RestrictionDescriptor restriction = action.getRestriction();
        if (restriction != null) {
            collectConditions(restriction.getConditionsDescriptor().getConditions(), scriptId, items);
        }

        int i = 1;
        for (Object cond : action.getValidators()) {
            ValidatorDescriptor condition = (ValidatorDescriptor) cond;

            Map args = condition.getArgs();

            if (isUsed(WorkflowScriptType.VALIDATOR, args, scriptId)) {
                items.add(new WorkflowActionItem(WorkflowScriptType.VALIDATOR, i));
            }

            ++i;
        }

        i = 1;
        ResultDescriptor unconditionalResult = action.getUnconditionalResult();
        if (unconditionalResult != null) {
            for (Object cond : unconditionalResult.getPostFunctions()) {
                FunctionDescriptor condition = (FunctionDescriptor) cond;

                Map args = condition.getArgs();

                if (isUsed(WorkflowScriptType.FUNCTION, args, scriptId)) {
                    items.add(new WorkflowActionItem(WorkflowScriptType.FUNCTION, i));
                }

                ++i;
            }
        }


        if (items.size() > 0) {
            return Stream.of(new WorkflowActionDto(
                action.getId(), action.getName(), items
            ));
        }

        return Stream.of();
    }

    private boolean isUsed(WorkflowScriptType type, Map args, int scriptId) {
        String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);
        String itemScriptId = (String) args.get(Const.WF_REPOSITORY_SCRIPT_ID);

        return Objects.equals(moduleKey, type.getModuleKey()) && Objects.equals(itemScriptId, String.valueOf(scriptId));
    }
}
