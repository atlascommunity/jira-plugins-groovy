package ru.mail.jira.plugins.groovy.impl.workflow.search;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.primitives.Ints;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.HashMap;
import java.util.Map;

public class AllScriptUsageCollector implements WorkflowSearchCollector {
    private final Multiset<Integer> counts = HashMultiset.create();
    private boolean skip = false;

    @Override
    public void setWorkflow(JiraWorkflow workflow) {
        skip = !workflow.isActive();
    }

    @Override
    public void workflowComplete() {
        skip = false;
    }

    @Override
    public void setAction(ActionDescriptor action) { }

    @Override
    public void actionComplete() { }

    @Override
    public void collect(ConditionDescriptor descriptor) {
        tryCount(WorkflowScriptType.CONDITION, descriptor.getArgs());
    }

    @Override
    public void collect(ValidatorDescriptor descriptor, int order) {
        tryCount(WorkflowScriptType.VALIDATOR, descriptor.getArgs());
    }

    @Override
    public void collect(FunctionDescriptor descriptor, int order) {
        tryCount(WorkflowScriptType.FUNCTION, descriptor.getArgs());
    }

    public Map<Integer, Integer> getResult() {
        Map<Integer, Integer> result = new HashMap<>();
        for (Integer key : counts) {
            result.put(key, counts.count(key));
        }
        return result;
    }

    private void tryCount(WorkflowScriptType scriptType, Map args) {
        if (skip) {
            return;
        }

        String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);
        String itemScriptId = (String) args.get(Const.WF_REPOSITORY_SCRIPT_ID);

        if (scriptType.getModuleKey().equals(moduleKey)) {
            Integer scriptId = Ints.tryParse(itemScriptId);

            if (scriptId != null) {
                counts.add(scriptId);
            }
        }
    }
}
