package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.repository.RestRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.entity.AuditCategory;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.rest.HttpMethod;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptForm;
import ru.mail.jira.plugins.groovy.api.dto.rest.Script;
import ru.mail.jira.plugins.groovy.api.entity.AuditAction;
import ru.mail.jira.plugins.groovy.api.entity.RestChangelog;
import ru.mail.jira.plugins.groovy.api.entity.RestScript;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RestRepositoryImpl implements RestRepository {
    private final ActiveObjects ao;
    private final I18nHelper i18nHelper;
    private final ChangelogHelper changelogHelper;
    private final ScriptService scriptService;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public RestRepositoryImpl(
        @ComponentImport ActiveObjects ao,
        @ComponentImport I18nHelper i18nHelper,
        ChangelogHelper changelogHelper,
        ScriptService scriptService,
        AuditLogRepository auditLogRepository
    ) {
        this.ao = ao;
        this.i18nHelper = i18nHelper;
        this.changelogHelper = changelogHelper;
        this.scriptService = scriptService;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public RestScriptDto getScript(int id, boolean includeChangelogs) {
        return buildScriptDto(ao.get(RestScript.class, id), includeChangelogs);
    }

    @Override
    public List<RestScriptDto> getAllScripts() {
        return Arrays
            .stream(ao.find(RestScript.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(script -> buildScriptDto(script, true))
            .collect(Collectors.toList());
    }

    @Override
    public RestScriptDto createScript(ApplicationUser user, RestScriptForm form) {
        validateScriptForm(true, form);

        RestScript script = ao.create(
            RestScript.class,
            new DBParam("NAME", form.getName()),
            new DBParam("UUID", UUID.randomUUID().toString()),
            new DBParam("METHODS", joinMethods(form.getMethods())),
            new DBParam("SCRIPT_BODY", form.getScriptBody()),
            new DBParam("DELETED", false)
        );

        String diff = changelogHelper.generateDiff(script.getID(), "", script.getName(), "", form.getScriptBody());

        changelogHelper.addChangelog(RestChangelog.class, script.getID(), user.getKey(), diff, "Created.");

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REST,
                AuditAction.CREATED,
                script.getID() + " - " + script.getName()
            )
        );

        return buildScriptDto(script, true);
    }

    @Override
    public RestScriptDto updateScript(ApplicationUser user, int id, RestScriptForm form) {
        validateScriptForm(false, form);

        RestScript script = ao.get(RestScript.class, id);

        String diff = changelogHelper.generateDiff(id, script.getName(), form.getName(), script.getScriptBody(), form.getScriptBody());

        changelogHelper.addChangelog(RestChangelog.class, script.getID(), user.getKey(), diff, form.getComment());

        script.setUuid(UUID.randomUUID().toString());
        script.setMethods(joinMethods(form.getMethods()));
        script.setName(form.getName());
        script.setScriptBody(form.getScriptBody());
        script.save();

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REST,
                AuditAction.UPDATED,
                script.getID() + " - " + script.getName()
            )
        );

        return buildScriptDto(script, true);
    }

    @Override
    public void deleteScript(ApplicationUser user, int id) {
        RestScript script = ao.get(RestScript.class, id);

        script.setDeleted(true);
        script.save();

        //todo: free name after deletion

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REST,
                AuditAction.UPDATED,
                script.getID() + " - " + script.getName()
            )
        );
    }

    @Override
    public Script getScript(HttpMethod method, String name) {
        //todo: cache?
        RestScript[] restScripts = ao
            .find(
                RestScript.class,
                Query.select().where("NAME = ? AND DELETED = ?", name, Boolean.FALSE)
            );

        if (restScripts.length == 0) {
            return null;
        }

        RestScript script = restScripts[0];

        if (parseMethods(script.getMethods()).stream().noneMatch(method::equals)) {
            return null;
        }

        return new Script(
            script.getUuid(),
            script.getScriptBody()
        );
    }

    private void validateScriptForm(boolean isNew, RestScriptForm form) {
        scriptService.parseScript(form.getScriptBody());

        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        if (!Const.REST_NAME_PATTERN.matcher(form.getName()).matches()) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.incorrectRestName"), "name");
        }

        //todo: check if name is taken

        if (StringUtils.isEmpty(form.getScriptBody())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "scriptBody");
        }

        if (form.getMethods() == null || form.getMethods().isEmpty()) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "methods");
        }

        if (!isNew) {
            if (StringUtils.isEmpty(form.getComment())) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }

            if (form.getComment().length() > Const.COMMENT_MAX_LENGTH) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "comment");
            }
        }
    }

    private RestScriptDto buildScriptDto(RestScript script, boolean includeChangelogs) {
        RestScriptDto result = new RestScriptDto();

        result.setId(script.getID());
        result.setUuid(script.getUuid());
        result.setName(script.getName());
        result.setMethods(parseMethods(script.getMethods()));
        result.setScriptBody(script.getScriptBody());
        result.setDeleted(script.isDeleted());

        if (includeChangelogs) {
            result.setChangelogs(changelogHelper.collect(script.getChangelogs()));
        }

        return result;
    }

    private String joinMethods(Set<HttpMethod> methods) {
        return methods.stream().map(HttpMethod::name).collect(Collectors.joining(","));
    }

    private Set<HttpMethod> parseMethods(String methods) {
        return Arrays.stream(methods.split(",")).map(HttpMethod::valueOf).collect(Collectors.toSet());
    }
}
