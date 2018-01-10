package ru.mail.jira.plugins.groovy.impl.workflow.registry;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.templaterenderer.JavaScriptEscaper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.dto.ScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.entity.Script;
import ru.mail.jira.plugins.groovy.api.entity.ScriptDirectory;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.JsonMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Scanned
public abstract class RegistryScriptWorkflowPluginFactory extends AbstractWorkflowPluginFactory {
    private final ScriptRepository scriptRepository;
    private final JsonMapper jsonMapper;

    protected RegistryScriptWorkflowPluginFactory(ScriptRepository scriptRepository, JsonMapper jsonMapper) {
        this.scriptRepository = scriptRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> map) {
        map.put("scripts", scriptRepository.getAllScriptDescriptions());
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        Map<String, Object> args = getArgs(abstractDescriptor);

        map.put("scripts", scriptRepository.getAllScriptDescriptions());
        map.put("id", args.get(Const.WF_REPOSITORY_SCRIPT_ID));
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);
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
            Script script = scriptRepository.getRawScript(id);

            if (script != null) {
                map.put("script", script);

                List<String> nameElements = new ArrayList<>();
                nameElements.add(script.getName());

                ScriptDirectory directory = script.getDirectory();
                while (directory != null) {
                    nameElements.add(directory.getName());
                    directory = directory.getParent();
                }

                map.put("extendedName", Lists.reverse(nameElements).stream().collect(Collectors.joining("/")));

                if (script.getParameters() != null) {
                    List<ScriptParamDto> params = jsonMapper.read(script.getParameters(), Const.PARAM_LIST_TYPE_REF);
                    Map<String, String> paramValues = new HashMap<>();

                    for (ScriptParamDto param : params) {
                        String paramName = param.getName();
                        paramValues.put(paramName, (String) args.get(Const.getParamKey(paramName)));
                    }

                    map.put("params", params);
                    map.put("paramValues", paramValues);
                }
            }
        }
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> input) {
        Map<String, Object> params = new HashMap<>();

        String scriptIdString = extractSingleParam(input, "script");
        params.put(Const.WF_REPOSITORY_SCRIPT_ID, scriptIdString);

        Integer scriptId = Ints.tryParse(scriptIdString);

        if (scriptId == null) {
            throw new RuntimeException("script is not a number");
        }

        ScriptDto script = scriptRepository.getScript(scriptId);

        if (script.getParams() != null) {
            for (ScriptParamDto scriptParamDto : script.getParams()) {
                String paramName = scriptParamDto.getName();
                String value = extractSingleParam(input, "script-" + paramName);
                if (value == null) {
                    throw new RuntimeException("param for " + paramName + " is not specified");
                }
                params.put(Const.getParamKey(paramName), value);
            }
        }

        return params;
    }

    abstract protected Map<String, Object> getArgs(AbstractDescriptor descriptor);
}
