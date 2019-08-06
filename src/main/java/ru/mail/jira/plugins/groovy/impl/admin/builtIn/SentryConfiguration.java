package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.service.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.api.service.SentryService;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.util.ValidationException;

import java.util.List;
import java.util.Map;

@Component
public class SentryConfiguration implements BuiltInScript<String> {
    private final ScriptInvalidationService scriptInvalidationService;
    private final SentryService sentryService;

    @Autowired
    public SentryConfiguration(
        ScriptInvalidationService scriptInvalidationService,
        SentryService sentryService
    ) {
        this.scriptInvalidationService = scriptInvalidationService;
        this.sentryService = sentryService;
    }

    @Override
    public String run(
        ApplicationUser currentUser, Map<String, Object> params
    ) throws Exception {
        Boolean enabled = (Boolean) params.get("enabled");
        String dsn = (String) params.get("dsn");

        if (enabled == null) {
            throw new ValidationException("\"enabled\" is required");
        }

        if (enabled && dsn == null) {
            throw new ValidationException("DSN is required to enable sentry integration");
        }

        sentryService.updateSettings(enabled, dsn);
        scriptInvalidationService.invalidateSentry();
        return "ok";
    }

    @Override
    public String getKey() {
        return "configureSentry";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.configureSentry";
    }

    @Override
    public boolean isHtml() {
        return false;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("enabled", "Enable", ParamType.BOOLEAN, false),
            new ScriptParamDto("dsn", "DSN", ParamType.STRING, true)
        );
    }
}
