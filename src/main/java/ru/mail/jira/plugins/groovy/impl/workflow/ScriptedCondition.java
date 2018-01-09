package ru.mail.jira.plugins.groovy.impl.workflow;

import com.atlassian.jira.workflow.condition.AbstractJiraCondition;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import java.util.Map;

@Scanned
public class ScriptedCondition extends AbstractJiraCondition {
    private final Logger logger = LoggerFactory.getLogger(ScriptedCondition.class);
    private final WorkflowHelper workflowHelper;

    public ScriptedCondition(WorkflowHelper workflowHelper) {
        this.workflowHelper = workflowHelper;
    }

    @Override
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        ScriptDescriptor script = workflowHelper.getScript(args);

        if (script == null) {
            logger.error("script must be present");
            return false;
        }

        Object result = workflowHelper.executeScript(script, ScriptType.WORKFLOW_CONDITION, getIssue(transientVars), getCallerUser(transientVars, args), transientVars);

        if (result instanceof Boolean) {
            return (boolean) result;
        } else {
            logger.error("Script condition must return boolean value");
        }

        return false;
    }
}
