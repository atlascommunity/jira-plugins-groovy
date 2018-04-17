package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.RestFieldException;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.RestRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.rest.HttpMethod;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptForm;
import ru.mail.jira.plugins.groovy.api.dto.rest.Script;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
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
    private final GroupManager groupManager;
    private final ChangelogHelper changelogHelper;
    private final ScriptService scriptService;
    private final AuditLogRepository auditLogRepository;
    private final ExecutionRepository executionRepository;

    @Autowired
    public RestRepositoryImpl(
        @ComponentImport ActiveObjects ao,
        @ComponentImport I18nHelper i18nHelper,
        @ComponentImport GroupManager groupManager,
        ChangelogHelper changelogHelper,
        ScriptService scriptService,
        AuditLogRepository auditLogRepository,
        ExecutionRepository executionRepository
    ) {
        this.ao = ao;
        this.i18nHelper = i18nHelper;
        this.groupManager = groupManager;
        this.changelogHelper = changelogHelper;
        this.scriptService = scriptService;
        this.auditLogRepository = auditLogRepository;
        this.executionRepository = executionRepository;
    }

    @Override
    public RestScriptDto getScript(int id, boolean includeChangelogs, boolean includeErrorCount) {
        return buildScriptDto(ao.get(RestScript.class, id), includeChangelogs, includeErrorCount);
    }

    @Override
    public List<RestScriptDto> getAllScripts() {
        return Arrays
            .stream(ao.find(RestScript.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(script -> buildScriptDto(script, true, true))
            .collect(Collectors.toList());
    }

    @Override
    public RestScriptDto createScript(ApplicationUser user, RestScriptForm form) {
        validateScriptForm(true, form, null);

        RestScript script = ao.create(
            RestScript.class,
            new DBParam("NAME", form.getName()),
            new DBParam("UUID", UUID.randomUUID().toString()),
            new DBParam("METHODS", joinMethods(form.getMethods())),
            new DBParam("SCRIPT_BODY", form.getScriptBody()),
            new DBParam("GROUPS", form.getGroups().stream().collect(Collectors.joining(","))),
            new DBParam("DELETED", false)
        );

        String diff = changelogHelper.generateDiff(script.getID(), "", script.getName(), "", form.getScriptBody());

        String comment = form.getComment();
        if (comment == null) {
            comment = "Created.";
        }

        changelogHelper.addChangelog(RestChangelog.class, script.getID(), user.getKey(), diff, comment);

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                EntityType.REST,
                script.getID(),
                EntityAction.CREATED,
                comment
            )
        );

        return buildScriptDto(script, true, true);
    }

    @Override
    public RestScriptDto updateScript(ApplicationUser user, int id, RestScriptForm form) {
        RestScript script = ao.get(RestScript.class, id);

        validateScriptForm(false, form, script.getName());

        String diff = changelogHelper.generateDiff(id, script.getName(), form.getName(), script.getScriptBody(), form.getScriptBody());
        String comment = form.getComment();

        changelogHelper.addChangelog(RestChangelog.class, script.getID(), user.getKey(), diff, comment);

        script.setUuid(UUID.randomUUID().toString());
        script.setMethods(joinMethods(form.getMethods()));
        script.setGroups(form.getGroups().stream().collect(Collectors.joining(",")));
        script.setName(form.getName());
        script.setScriptBody(form.getScriptBody());
        script.save();

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                EntityType.REST,
                script.getID(),
                EntityAction.UPDATED,
                comment
            )
        );

        return buildScriptDto(script, true, true);
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
                EntityType.REST,
                script.getID(),
                EntityAction.DELETED,
                script.getID() + " - " + script.getName()
            )
        );
    }

    @Override
    public void restoreScript(ApplicationUser user, int id) {
        RestScript script = ao.get(RestScript.class, id);

        script.setDeleted(false);
        script.save();

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                EntityType.REST,
                script.getID(),
                EntityAction.RESTORED,
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
            script.getScriptBody(),
            parseGroups(script.getGroups())
        );
    }

    private void validateScriptForm(boolean isNew, RestScriptForm form, String oldName) {
        scriptService.parseScript(form.getScriptBody());

        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        if (!Const.REST_NAME_PATTERN.matcher(form.getName()).matches()) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.incorrectRestName"), "name");
        }

        if ((isNew || !form.getName().equals(oldName)) && !isNameAvailable(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.restNameTaken"), "name");
        }

        if (StringUtils.isEmpty(form.getScriptBody())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "scriptBody");
        }

        if (form.getMethods() == null || form.getMethods().isEmpty()) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "methods");
        }

        if (form.getGroups() == null) {
            form.setGroups(ImmutableSet.of());
        }

        if (!form.getGroups().isEmpty()) {
            if (form.getGroups().size() > 10) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.tooManyGroups"), "groups");
            }

            for (String groupName : form.getGroups()) {
                if (groupManager.getGroup(groupName) == null) {
                    throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.unknownGroup", groupName), "groups");
                }
            }
        }

        if (!isNew) {
            if (StringUtils.isEmpty(form.getComment())) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }
        }

        if (form.getComment() != null) {
            if (form.getComment().length() > Const.COMMENT_MAX_LENGTH) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "comment");
            }
        }
    }

    private boolean isNameAvailable(String name) {
        return ao.count(RestScript.class, Query.select().where("NAME = ?", name)) == 0;
    }

    private RestScriptDto buildScriptDto(RestScript script, boolean includeChangelogs, boolean includeErrorCount) {
        RestScriptDto result = new RestScriptDto();

        result.setId(script.getID());
        result.setUuid(script.getUuid());
        result.setName(script.getName());
        result.setMethods(parseMethods(script.getMethods()));
        result.setGroups(parseGroups(script.getGroups()));
        result.setScriptBody(script.getScriptBody());
        result.setDeleted(script.isDeleted());

        if (includeChangelogs) {
            result.setChangelogs(changelogHelper.collect(script.getChangelogs()));
        }

        if (includeErrorCount) {
            result.setErrorCount(executionRepository.getErrorCount(script.getUuid()));
        }

        return result;
    }

    private String joinMethods(Set<HttpMethod> methods) {
        return methods.stream().map(HttpMethod::name).collect(Collectors.joining(","));
    }

    private Set<HttpMethod> parseMethods(String methods) {
        return Arrays.stream(methods.split(",")).map(HttpMethod::valueOf).collect(Collectors.toSet());
    }

    private Set<String> parseGroups(String groups) {
        groups = StringUtils.trimToNull(groups);

        if (groups == null) {
            return ImmutableSet.of();
        }

        return Arrays.stream(groups.split(",")).collect(Collectors.toSet());
    }
}
