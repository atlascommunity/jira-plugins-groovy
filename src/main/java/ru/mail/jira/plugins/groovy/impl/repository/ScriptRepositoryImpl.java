package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.groovy.api.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.*;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditCategory;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.*;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.JsonMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScriptRepositoryImpl implements ScriptRepository {
    private final I18nHelper i18nHelper;
    private final ClusterLockService clusterLockService;
    private final ActiveObjects ao;
    private final ScriptInvalidationService scriptInvalidationService;
    private final ScriptService scriptService;
    private final JsonMapper jsonMapper;
    private final AuditLogRepository auditLogRepository;
    private final ChangelogHelper changelogHelper;

    @Autowired
    public ScriptRepositoryImpl(
        @ComponentImport I18nHelper i18nHelper,
        @ComponentImport ClusterLockService clusterLockService,
        @ComponentImport ActiveObjects ao,
        ScriptInvalidationService scriptInvalidationService,
        ScriptService scriptService,
        JsonMapper jsonMapper,
        AuditLogRepository auditLogRepository,
        ChangelogHelper changelogHelper
    ) {
        this.i18nHelper = i18nHelper;
        this.clusterLockService = clusterLockService;
        this.ao = ao;
        this.scriptInvalidationService = scriptInvalidationService;
        this.scriptService = scriptService;
        this.jsonMapper = jsonMapper;
        this.auditLogRepository = auditLogRepository;
        this.changelogHelper = changelogHelper;
    }

    private ScriptDirectory getParentDirectory(Integer parentId) {
        ScriptDirectory parentDirectory = null;

        if (parentId != null) {
            parentDirectory = ao.get(ScriptDirectory.class, parentId);

            if (parentDirectory == null) {
                throw new IllegalArgumentException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.directoryNotFound", parentId));
            } else if (parentDirectory.isDeleted()) {
                throw new IllegalArgumentException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.parentDirectoryIsDeleted"));
            }
        }

        return parentDirectory;
    }

    @Override
    public List<ScriptDirectoryTreeDto> getAllDirectories() {
        Multimap<Integer, RegistryScriptDto> scripts = HashMultimap.create();
        for (RegistryScriptDto scriptDto : getAllScripts(true)) {
            scripts.put(scriptDto.getDirectoryId(), scriptDto);
        }

        return Arrays
            .stream(ao.find(ScriptDirectory.class, Query.select().where("DELETED = ? AND PARENT_ID IS NULL", Boolean.FALSE)))
            .map(directory -> buildDirectoryTreeDto(directory, scripts))
            .collect(Collectors.toList());
    }

    @Override
    public ScriptDirectoryDto getDirectory(int id) {
        return buildDirectoryDto(ao.get(ScriptDirectory.class, id));
    }

    @Override
    public ScriptDirectoryDto createDirectory(ApplicationUser user, ScriptDirectoryForm form) {
        validateDirectoryForm(form);

        ScriptDirectory directory = ao.create(
            ScriptDirectory.class,
            new DBParam("NAME", form.getName()),
            new DBParam("PARENT_ID", getParentDirectory(form.getParentId())),
            new DBParam("DELETED", false)
        );

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REGISTRY_DIRECTORY,
                AuditAction.CREATED,
                directory.getID() + " - " + directory.getName()
            )
        );

        return buildDirectoryDto(directory);
    }

    @Override
    public ScriptDirectoryDto updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm form) {
        ScriptDirectory directory = ao.get(ScriptDirectory.class, id);
        directory.setName(form.getName());
        directory.setParent(getParentDirectory(form.getParentId()));
        directory.save();

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REGISTRY_DIRECTORY,
                AuditAction.UPDATED,
                directory.getID() + " - " + directory.getName()
            )
        );

        return buildDirectoryDto(directory);
    }

    @Override
    public void deleteDirectory(ApplicationUser user, int id) {
        deleteDirectory(user, ao.get(ScriptDirectory.class, id));
    }

    private void deleteDirectory(ApplicationUser user, ScriptDirectory directory) {
        directory.setDeleted(true);
        directory.save();

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REGISTRY_DIRECTORY,
                AuditAction.DELETED,
                directory.getID() + " - " + directory.getName()
            )
        );

        for (ScriptDirectory child : directory.getChildren()) {
            deleteDirectory(user, child);
        }

        for (Script script : directory.getScripts()) {
            deleteScript(user, script);
        }
    }

    @Override
    public List<RegistryScriptDto> getAllScripts(boolean includeChangelog) {
        return Arrays
            .stream(ao.find(Script.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(script -> buildScriptDto(script, includeChangelog, false))
            .collect(Collectors.toList());
    }

    @Override
    public List<ScriptDescription> getAllScriptDescriptions() {
        return Arrays
            .stream(ao.find(Script.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(this::buildScriptDescription)
            .sorted(Comparator.comparing(ScriptDescription::getName))
            .collect(Collectors.toList());
    }

    @Override
    public RegistryScriptDto getScript(int id, boolean includeChangelogs, boolean expandName) {
        return buildScriptDto(ao.get(Script.class, id), includeChangelogs, expandName);
    }

    @Override
    public RegistryScriptDto createScript(ApplicationUser user, RegistryScriptForm scriptForm) {
        if (scriptForm.getDirectoryId() == null) {
            throw new RuntimeException();
        }

        ParseContext parseContext = validateScriptForm(true, scriptForm);

        String parameters = parseContext.getParameters().size() > 0 ? jsonMapper.write(parseContext.getParameters()) : null;

        Script script = ao.create(
            Script.class,
            new DBParam("NAME", scriptForm.getName()),
            new DBParam("SCRIPT_BODY", scriptForm.getScriptBody()),
            new DBParam("DIRECTORY_ID", scriptForm.getDirectoryId()),
            new DBParam("DELETED", false),
            new DBParam("PARAMETERS", parameters)
        );

        String diff = changelogHelper.generateDiff(script.getID(), "", script.getName(), "", scriptForm.getScriptBody());

        changelogHelper.addChangelog(Changelog.class, script.getID(), user.getKey(), diff, "Created.");

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REGISTRY_SCRIPT,
                AuditAction.CREATED,
                script.getID() + " - " + script.getName()
            )
        );

        return buildScriptDto(script, true, false);
    }

    @Override
    public RegistryScriptDto updateScript(ApplicationUser user, int id, RegistryScriptForm scriptForm) {
        ParseContext parseContext = validateScriptForm(false, scriptForm);

        ClusterLock lock = clusterLockService.getLockForName(getLockKey(id));

        lock.lock();
        try {
            RegistryScriptDto result = doUpdateScript(user, id, scriptForm, parseContext);
            scriptInvalidationService.invalidate(String.valueOf(id));
            return result;
        } finally {
            lock.unlock();
        }
    }

    private RegistryScriptDto doUpdateScript(ApplicationUser user, int id, RegistryScriptForm form, ParseContext parseContext) {
        Script script = ao.get(Script.class, id);

        if (script.isDeleted()) {
            throw new IllegalArgumentException("Script " + id + " is deleted");
        }

        String diff = changelogHelper.generateDiff(id, script.getName(), form.getName(), script.getScriptBody(), form.getScriptBody());

        changelogHelper.addChangelog(Changelog.class, script.getID(), user.getKey(), diff, form.getComment());

        String parameters = parseContext.getParameters().size() > 0 ? jsonMapper.write(parseContext.getParameters()) : null;

        script.setName(form.getName());
        script.setScriptBody(form.getScriptBody());
        script.setParameters(parameters);
        script.save();

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REGISTRY_SCRIPT,
                AuditAction.UPDATED,
                script.getID() + " - " + script.getName()
            )
        );

        return buildScriptDto(script, true, false);
    }

    @Override
    public void deleteScript(ApplicationUser user, int id) {
        ClusterLock lock = clusterLockService.getLockForName(getLockKey(id));

        lock.lock();
        try {
            deleteScript(user, ao.get(Script.class, id));
        } finally {
            lock.unlock();
        }
    }

    private void deleteScript(ApplicationUser user, Script script) {
        script.setDeleted(true);
        script.save();

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.REGISTRY_SCRIPT,
                AuditAction.DELETED,
                script.getID() + " - " + script.getName()
            )
        );
    }

    private RegistryScriptDto buildScriptDto(Script script, boolean includeChangelogs, boolean expandName) {
        RegistryScriptDto result = new RegistryScriptDto();

        result.setId(script.getID());
        result.setDirectoryId(script.getDirectory().getID());
        result.setScriptBody(script.getScriptBody());
        result.setDeleted(script.isDeleted());

        if (expandName) {
            result.setName(getExpandedName(script));
        } else {
            result.setName(script.getName());
        }
        if (includeChangelogs) {
            Changelog[] changelogs = script.getChangelogs();
            if (changelogs != null) {
                result.setChangelogs(
                    Arrays
                        .stream(changelogs)
                        .sorted(Comparator.comparing(Changelog::getDate).reversed())
                        .map(changelogHelper::buildDto)
                        .collect(Collectors.toList())
                );
            }
        }

        if (script.getParameters() != null) {
            result.setParams(jsonMapper.read(script.getParameters(), Const.PARAM_LIST_TYPE_REF));
        }

        return result;
    }

    private static ScriptDirectoryDto buildDirectoryDto(ScriptDirectory directory) {
        ScriptDirectoryDto result = new ScriptDirectoryDto();

        result.setId(directory.getID());
        result.setName(directory.getName());

        ScriptDirectory parent = directory.getParent();
        if (parent != null) {
            result.setParentId(parent.getID());
        }

        return result;
    }

    private static ScriptDirectoryTreeDto buildDirectoryTreeDto(ScriptDirectory directory, Multimap<Integer, RegistryScriptDto> scripts) {
        ScriptDirectoryTreeDto result = new ScriptDirectoryTreeDto();

        result.setId(directory.getID());
        result.setName(directory.getName());
        result.setChildren(
            Arrays
                .stream(directory.getChildren())
                .map(child -> buildDirectoryTreeDto(child, scripts))
                .collect(Collectors.toList())
        );
        result.setScripts(scripts.get(directory.getID()).stream().sorted(Comparator.comparing(RegistryScriptDto::getId)).collect(Collectors.toList()));

        return result;
    }

    private void validateDirectoryForm(ScriptDirectoryForm form) {
        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }
    }

    private ParseContext validateScriptForm(boolean isNew, RegistryScriptForm form) {
        ParseContext parseContext = scriptService.parseScript(form.getScriptBody());

        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        if (StringUtils.isEmpty(form.getScriptBody())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "scriptBody");
        }

        if (!isNew) {
            if (StringUtils.isEmpty(form.getComment())) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }
        }

        return parseContext;
    }

    private ScriptDescription buildScriptDescription(Script script) {
        ScriptDescription result = new ScriptDescription();
        result.setId(script.getID());
        result.setName(getExpandedName(script));

        if (script.getParameters() != null) {
            result.setParams(jsonMapper.read(script.getParameters(), Const.PARAM_LIST_TYPE_REF));
        }

        return result;
    }

    private String getExpandedName(Script script) {
        List<String> nameElements = new ArrayList<>();
        nameElements.add(script.getName());

        ScriptDirectory directory = script.getDirectory();
        while (directory != null) {
            nameElements.add(directory.getName());
            directory = directory.getParent();
        }

        return Lists.reverse(nameElements).stream().collect(Collectors.joining("/"));
    }

    private static String getLockKey(int id) {
        return ScriptRepositoryImpl.class.toString() + "_script_" + id;
    }
}
