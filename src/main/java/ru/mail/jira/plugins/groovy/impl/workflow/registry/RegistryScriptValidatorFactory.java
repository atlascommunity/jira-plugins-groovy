package ru.mail.jira.plugins.groovy.impl.workflow.registry;

import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;

import java.util.Map;

@Scanned
public class RegistryScriptValidatorFactory extends RegistryScriptWorkflowPluginFactory implements WorkflowPluginValidatorFactory {
    public RegistryScriptValidatorFactory(ScriptRepository scriptRepository) {
        super(scriptRepository);
    }

    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((ValidatorDescriptor) descriptor).getArgs();
    }
}
