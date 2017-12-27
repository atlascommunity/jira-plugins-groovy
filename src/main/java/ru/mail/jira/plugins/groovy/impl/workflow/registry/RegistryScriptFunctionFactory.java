package ru.mail.jira.plugins.groovy.impl.workflow.registry;

import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;

import java.util.Map;

@Scanned
public class RegistryScriptFunctionFactory extends RegistryScriptWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
    public RegistryScriptFunctionFactory(ScriptRepository scriptRepository) {
        super(scriptRepository);
    }

    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((FunctionDescriptor) descriptor).getArgs();
    }
}
