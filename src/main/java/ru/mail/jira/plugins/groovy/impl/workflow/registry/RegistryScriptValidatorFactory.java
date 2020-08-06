package ru.mail.jira.plugins.groovy.impl.workflow.registry;

import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.script.ScriptParamFactory;
import ru.mail.jira.plugins.groovy.util.JsonMapper;

import java.util.Map;

@Scanned
public class RegistryScriptValidatorFactory extends RegistryScriptWorkflowPluginFactory implements WorkflowPluginValidatorFactory {
    public RegistryScriptValidatorFactory(ScriptRepository scriptRepository, ScriptParamFactory paramFactory, JsonMapper jsonMapper) {
        super(scriptRepository, paramFactory, jsonMapper);
    }

    @Override
    protected WorkflowScriptType getType() {
        return WorkflowScriptType.VALIDATOR;
    }

    @Override
    protected Map<String, Object> getArgs(AbstractDescriptor descriptor) {
        return ((ValidatorDescriptor) descriptor).getArgs();
    }
}
