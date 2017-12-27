package ru.mail.jira.plugins.groovy.impl.workflow;

import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ScriptDescriptor script = workflowHelper.getScript(args);

        if (script == null) {
            logger.error("script must be present");
            return;
        }

        workflowHelper.executeScript(script, getIssue(transientVars), getCallerUser(transientVars, args), transientVars);
    }
}
