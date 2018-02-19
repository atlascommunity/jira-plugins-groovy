package ru.mail.jira.plugins.groovy.impl.workflow;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import java.util.Map;

@Scanned
public class ScriptedValidator implements Validator {
    private final Logger logger = LoggerFactory.getLogger(ScriptedFunction.class);
    private final WorkflowHelper workflowHelper;

    public ScriptedValidator(WorkflowHelper workflowHelper) {
        this.workflowHelper = workflowHelper;
    }

    @Override
    public void validate(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        ScriptDescriptor script = workflowHelper.getScript(args, WorkflowScriptType.VALIDATOR);

        if (script == null) {
            logger.error("script must be present");
            return;
        }

        workflowHelper.executeScript(script, ScriptType.WORKFLOW_VALIDATOR, (Issue) transientVars.get("issue"), getCaller(transientVars), transientVars);
    }

    private ApplicationUser getCaller(Map transientVars) throws InvalidInputException {
        String userKey = this.getCallerKey(transientVars);
        ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(userKey);
        if (userKey != null && user == null) {
            throw new InvalidInputException("You don't have the correct permissions - user (" + userKey + ") not found");
        } else {
            return user;
        }
    }

    private String getCallerKey(Map transientVars) {
        return WorkflowUtil.getCallerKey(transientVars);
    }
}
