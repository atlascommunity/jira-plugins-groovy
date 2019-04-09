package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.repository.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.impl.cf.FieldValueExtractor;

import java.util.List;
import java.util.Map;

@Component
public class ClearCache implements BuiltInScript<String> {
    private final ScriptInvalidationService scriptInvalidationService;
    private final EventListenerRepository listenerRepository;
    private final FieldValueExtractor fieldValueExtractor;
    private final FieldConfigRepository fieldConfigRepository;

    @Autowired
    public ClearCache(
        ScriptInvalidationService scriptInvalidationService,
        EventListenerRepository listenerRepository,
        FieldValueExtractor fieldValueExtractor,
        FieldConfigRepository fieldConfigRepository
    ) {
        this.scriptInvalidationService = scriptInvalidationService;
        this.listenerRepository = listenerRepository;
        this.fieldValueExtractor = fieldValueExtractor;
        this.fieldConfigRepository = fieldConfigRepository;
    }

    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) throws Exception {
        scriptInvalidationService.invalidateAllFields();
        scriptInvalidationService.invalidateAll();
        scriptInvalidationService.invalidateGlobalObjects();
        listenerRepository.invalidate();

        fieldConfigRepository.invalidateAll();
        fieldValueExtractor.clearCache();

        return "done";
    }

    @Override
    public String getKey() {
        return "clearMyGroovyCache";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.clearCache";
    }

    @Override
    public boolean isHtml() {
        return false;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of();
    }
}
