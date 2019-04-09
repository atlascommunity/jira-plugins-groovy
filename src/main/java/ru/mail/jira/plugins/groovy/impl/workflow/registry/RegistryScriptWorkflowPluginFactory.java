package ru.mail.jira.plugins.groovy.impl.workflow.registry;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.templaterenderer.JavaScriptEscaper;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.dto.directory.RegistryScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.impl.param.ScriptParamFactory;
import ru.mail.jira.plugins.groovy.util.Base64Util;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.JsonMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class RegistryScriptWorkflowPluginFactory extends AbstractWorkflowPluginFactory {
    private final ScriptRepository scriptRepository;
    private final ScriptParamFactory paramFactory;
    private final JsonMapper jsonMapper;

    protected RegistryScriptWorkflowPluginFactory(ScriptRepository scriptRepository, ScriptParamFactory paramFactory, JsonMapper jsonMapper) {
        this.scriptRepository = scriptRepository;
        this.paramFactory = paramFactory;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> map) {
        map.put("type", getType());
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);
        map.put("values", jsonMapper.write(ImmutableMap.of()));
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        Map<String, Object> args = getArgs(abstractDescriptor);

        String idString = (String) args.get(Const.WF_REPOSITORY_SCRIPT_ID);
        Integer scriptId = Ints.tryParse(idString);
        RegistryScriptDto script = null;
        if (scriptId != null) {
            script = scriptRepository.getScript(scriptId, false, false, false);
        }

        map.put("type", getType());
        map.put("id", scriptId);
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);

        map.put("values", jsonMapper.write(getScriptParams(script, args)));
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        Map<String, Object> args = getArgs(abstractDescriptor);

        String idString = (String) args.get(Const.WF_REPOSITORY_SCRIPT_ID);
        map.put("uuid", UUID.randomUUID());
        map.put("id", idString);
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);

        Integer id = Ints.tryParse(idString);
        if (id != null) {
            RegistryScriptDto script = scriptRepository.getScript(id, false, true, true);

            if (script != null) {
                map.put("script", script);
                map.put("typeMatching", script.getTypes().contains(getType()));

                if (script.getParams() != null) {
                    map.put("paramsHtml", jsonMapper.write(script.getParams()));
                    map.put("paramValuesHtml", jsonMapper.write(getScriptParams(script, args)));
                }
            }
        }
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> input) {
        Map<String, Object> params = new HashMap<>();

        String scriptIdString = extractSingleParam(input, "script");
        params.put(Const.WF_REPOSITORY_SCRIPT_ID, scriptIdString);
        params.put(Const.JIRA_WF_FULL_MODULE_KEY, getType().getModuleKey());

        Integer scriptId = Ints.tryParse(scriptIdString);

        if (scriptId == null) {
            throw new RuntimeException("script is not a number");
        }

        RegistryScriptDto script = scriptRepository.getScript(scriptId, false, false, false);

        if (script.getParams() != null) {
            for (ScriptParamDto scriptParamDto : script.getParams()) {
                String paramName = scriptParamDto.getName();
                String paramKey = "script-" + paramName;

                String value = input.containsKey(paramKey) ? extractSingleParam(input, paramKey) : null;

                if (scriptParamDto.getParamType() == ParamType.BOOLEAN && value == null) {
                    value = "false";
                }

                value = StringUtils.trimToNull(value);
                if (value == null && !scriptParamDto.isOptional()) {
                    throw new RuntimeException("param for " + paramName + " is not specified");
                }

                params.put(Const.getParamKey(paramName), Base64Util.encode(value));
            }
        }

        return params;
    }

    private Map<String, Object> getScriptParams(RegistryScriptDto script, Map<String, Object> args) {
        Map<String, Object> values = new HashMap<>();
        if (script != null) {
            if (script.getParams() != null) {
                for (ScriptParamDto param : script.getParams()) {
                    String paramName = param.getName();
                    String value = StringUtils.trimToNull(Base64Util.decode((String) args.get(Const.getParamKey(paramName))));

                    values.put(paramName, paramFactory.getParamFormValue(param, value));
                }
            }
        }
        return values;
    }

    abstract protected WorkflowScriptType getType();

    abstract protected Map<String, Object> getArgs(AbstractDescriptor descriptor);
}
