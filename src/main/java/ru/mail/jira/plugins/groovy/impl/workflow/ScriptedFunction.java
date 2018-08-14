package ru.mail.jira.plugins.groovy.impl.workflow;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import java.util.Map;

@Scanned
public class ScriptedFunction extends AbstractJiraFunctionProvider {
    private final Logger logger = LoggerFactory.getLogger(ScriptedFunction.class);
    private final WorkflowHelper workflowHelper;

    public ScriptedFunction(WorkflowHelper workflowHelper) {
        this.workflowHelper = workflowHelper;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        ScriptDescriptor script = workflowHelper.getScript(args, WorkflowScriptType.FUNCTION, issue);

        if (script == null) {
            logger.error("script must be present");
            return;
        }

        workflowHelper.executeScript(script, ScriptType.WORKFLOW_FUNCTION, issue, getCallerUser(transientVars, args), transientVars);
    }
}
