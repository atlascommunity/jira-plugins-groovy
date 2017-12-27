package ru.mail.jira.plugins.groovy.impl.workflow.registry;

import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;

import java.util.Map;

@Scanned
public class RegistryScriptConditionFactory extends RegistryScriptWorkflowPluginFactory implements WorkflowPluginConditionFactory {
    public RegistryScriptConditionFactory(ScriptRepository scriptRepository) {
        super(scriptRepository);
    }

    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((ConditionDescriptor) descriptor).getArgs();
    }
}
