package ru.mail.jira.plugins.groovy.impl.admin;

import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptOutcome;
import ru.mail.jira.plugins.groovy.api.repository.AdminScriptRepository;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.admin.AdminScriptService;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScriptManager;
import ru.mail.jira.plugins.groovy.impl.ScriptParamFactory;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.ValidationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AdminScriptServiceImpl implements AdminScriptService {
    private final ScriptService scriptService;
    private final BuiltInScriptManager builtInScriptManager;
    private final AdminScriptRepository adminScriptRepository;
    private final ScriptParamFactory scriptParamFactory;

    @Autowired
    public AdminScriptServiceImpl(
        ScriptService scriptService,
        BuiltInScriptManager builtInScriptManager,
        AdminScriptRepository adminScriptRepository,
        ScriptParamFactory scriptParamFactory
    ) {
        this.scriptService = scriptService;
        this.builtInScriptManager = builtInScriptManager;
        this.adminScriptRepository = adminScriptRepository;
        this.scriptParamFactory = scriptParamFactory;
    }

    @Override
    public AdminScriptOutcome runBuiltInScript(ApplicationUser user, String key, Map<String, String> rawParams) {
        BuiltInScript script = builtInScriptManager.getScript(key);

        if (script == null) {
            throw new ValidationException("Script not found"); //todo: i18n
        }

        try {
            return new AdminScriptOutcome(true, script.run(user, getParams(script.getParams(), rawParams)));
        } catch (Exception e) {
            return new AdminScriptOutcome(false, ExceptionHelper.getMessageOrClassName(e));
        }
    }

    @Override
    public AdminScriptOutcome runUserScript(ApplicationUser user, Integer id, Map<String, String> rawParams) {
        AdminScriptDto script = adminScriptRepository.getScript(id, false, false);

        if (script == null || script.isDeleted()) {
            throw new ValidationException("Script not found"); //todo: i18n
        }

        //todo: current user binding
        try {
            return new AdminScriptOutcome(
                true,
                Objects.toString(
                    scriptService.executeScript(
                        null, script.getScriptBody(), ScriptType.ADMIN_SCRIPT,
                        getParams(script.getParams(), rawParams)
                    )
                )
            );
        } catch (Exception e) {
            return new AdminScriptOutcome(false, ExceptionHelper.getMessageOrClassName(e));
        }
    }

    private Map<String, Object> getParams(List<ScriptParamDto> params, Map<String, String> rawValues) {
        if (params == null || rawValues == null) {
            return ImmutableMap.of();
        }

        Map<String, Object> result = new HashMap<>();
        for (ScriptParamDto param : params) {
            result.put(param.getName(), scriptParamFactory.getParamObject(param, rawValues.get(param.getName())));
        }
        return result;
    }
}
