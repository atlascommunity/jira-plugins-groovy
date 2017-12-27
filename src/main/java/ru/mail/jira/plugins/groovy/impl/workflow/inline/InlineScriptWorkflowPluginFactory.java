package ru.mail.jira.plugins.groovy.impl.workflow.inline;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.templaterenderer.JavaScriptEscaper;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class InlineScriptWorkflowPluginFactory extends AbstractWorkflowPluginFactory {

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> map) {
        map.put("inlineScript", "");
        map.put("uuid", UUID.randomUUID().toString());
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);
        map.put("inlineScriptName", "");
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        Map<String, Object> args = getArgs(abstractDescriptor);

        map.put("inlineScript", args.getOrDefault(Const.WF_INLINE_SCRIPT, ""));
        map.put("inlineScriptName", args.getOrDefault(Const.WF_INLINE_SCRIPT_NAME, ""));
        map.put("uuid", UUID.randomUUID().toString()); //generate new uuid on edit
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        Map<String, Object> args = getArgs(abstractDescriptor);

        map.put("inlineScript", args.get(Const.WF_INLINE_SCRIPT));
        map.put("inlineScriptName", args.getOrDefault(Const.WF_INLINE_SCRIPT_NAME, ""));
        map.put("uuid", args.get(Const.WF_UUID));
        map.put("escapeJs", (Function<String, String>) JavaScriptEscaper::escape);
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> input) {
        Map<String, Object> params = new HashMap<>();
        
        params.put(Const.WF_INLINE_SCRIPT, extractSingleParam(input, "inlineScript"));
        params.put(Const.WF_INLINE_SCRIPT_NAME, extractSingleParam(input, "inlineScriptName"));
        params.put(Const.WF_UUID, extractSingleParam(input, "uuid"));

        return params;
    }

    abstract protected Map<String, Object> getArgs(AbstractDescriptor descriptor);
}
