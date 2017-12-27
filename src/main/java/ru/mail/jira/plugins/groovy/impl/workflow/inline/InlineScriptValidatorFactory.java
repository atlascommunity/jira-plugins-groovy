package ru.mail.jira.plugins.groovy.impl.workflow.inline;

import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Map;

public class InlineScriptValidatorFactory extends InlineScriptWorkflowPluginFactory implements WorkflowPluginValidatorFactory {
    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((ValidatorDescriptor) descriptor).getArgs();
    }
}
