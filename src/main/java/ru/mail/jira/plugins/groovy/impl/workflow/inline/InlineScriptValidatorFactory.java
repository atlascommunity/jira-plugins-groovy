package ru.mail.jira.plugins.groovy.impl.workflow.inline;

import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;

import java.util.Map;

public class InlineScriptValidatorFactory extends InlineScriptWorkflowPluginFactory implements WorkflowPluginValidatorFactory {
    public InlineScriptValidatorFactory(ExecutionRepository executionRepository) {
        super(executionRepository);
    }

    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((ValidatorDescriptor) descriptor).getArgs();
    }

    @Override
    protected String getModuleKey() {
        return "ru.mail.jira.plugins.groovyinline-script-validator";
    }
}
