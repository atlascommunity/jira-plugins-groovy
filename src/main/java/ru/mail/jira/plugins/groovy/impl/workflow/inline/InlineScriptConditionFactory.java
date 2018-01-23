package ru.mail.jira.plugins.groovy.impl.workflow.inline;

import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

import java.util.Map;

public class InlineScriptConditionFactory extends InlineScriptWorkflowPluginFactory implements WorkflowPluginConditionFactory {
    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((ConditionDescriptor) descriptor).getArgs();
    }

    @Override
    protected String getModuleKey() {
        return "ru.mail.jira.plugins.groovyinline-script-condition";
    }
}
