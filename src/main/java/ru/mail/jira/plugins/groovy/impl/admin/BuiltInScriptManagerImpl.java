package ru.mail.jira.plugins.groovy.impl.admin;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptDto;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScriptManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BuiltInScriptManagerImpl implements BuiltInScriptManager {
    private final Map<String, BuiltInScript> builtInScripts;

    private final I18nHelper i18nHelper;

    @Autowired
    public BuiltInScriptManagerImpl(
        @ComponentImport I18nHelper i18nHelper,
        List<BuiltInScript> scripts
    ) {
        this.i18nHelper = i18nHelper;

        ImmutableMap.Builder<String, BuiltInScript> scriptsBuilder = ImmutableMap.builder();
        for (BuiltInScript script : scripts) {
            scriptsBuilder.put(script.getKey(), script);
        }
        this.builtInScripts = scriptsBuilder.build();
    }

    @Override
    public List<AdminScriptDto> getAllScripts() {
        int id = -1;

        List<AdminScriptDto> list = new ArrayList<>();
        for (String key : builtInScripts.keySet()) {
            BuiltInScript script = builtInScripts.get(key);
            AdminScriptDto adminScriptDto = buildScriptDto(key, script.isHtml(), id--);
            list.add(adminScriptDto);
        }
        return list;
    }

    @Override
    public BuiltInScript getScript(String key) {
        return builtInScripts.get(key);
    }

    private AdminScriptDto buildScriptDto(String key, boolean isHtml, int id) {
        BuiltInScript script = builtInScripts.get(key);

        AdminScriptDto result = new AdminScriptDto();
        result.setId(id);
        result.setBuiltIn(true);
        result.setBuiltInKey(key);
        result.setHtml(isHtml);
        result.setName(i18nHelper.getText(script.getI18nKey()));
        result.setParams(script.getParams());
        result.setResultWidth(script.getResultWidth());

        return result;
    }
}
