package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.plugin.Plugin;
import ru.mail.jira.plugins.groovy.api.dto.CacheStatsDto;
import ru.mail.jira.plugins.groovy.api.script.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.script.ParseContext;
import ru.mail.jira.plugins.groovy.api.util.WithPluginLoader;

import java.util.Map;

public interface ScriptService {
    @WithPluginLoader
    Object executeScript(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings) throws Exception;

    @WithPluginLoader
    Object executeScriptStatic(String scriptId, String scriptString, ScriptType type, Map<String, Object> bindings, Map<String, Class> types) throws Exception;

    @WithPluginLoader
    ParseContext parseScript(String script);

    @WithPluginLoader
    ParseContext parseScriptStatic(String script, Map<String, Class> types);

    @WithPluginLoader
    Class parseClass(String classBody, boolean extended);

    @WithPluginLoader
    Class parseClassStatic(String classBody, boolean extended, Map<String, Class> types);

    CacheStatsDto getCacheStats();

    Map<String, Class> getGlobalBindingTypes();

    void registerBindingProvider(BindingProvider bindingProvider);

    void invalidate(String id);

    void invalidateAll();

    void onPluginDisable(Plugin event);
}
