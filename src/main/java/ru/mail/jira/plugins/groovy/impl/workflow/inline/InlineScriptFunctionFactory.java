package ru.mail.jira.plugins.groovy.impl.workflow.inline;

import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;

import java.util.Map;

public class InlineScriptFunctionFactory extends InlineScriptWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
    public InlineScriptFunctionFactory(ExecutionRepository executionRepository) {
        super(executionRepository);
    }

    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((FunctionDescriptor) descriptor).getArgs();
    }

    @Override
    protected String getModuleKey() {
        return "ru.mail.jira.plugins.groovyinline-script-function";
    }
}
