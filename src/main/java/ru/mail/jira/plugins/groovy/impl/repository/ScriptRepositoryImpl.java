package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.*;
import ru.mail.jira.plugins.groovy.api.dto.notification.NotificationDto;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.service.NotificationService;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.*;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.*;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.api.service.WatcherService;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;

import java.util.*;
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
    private final ExecutionRepository executionRepository;
    private final ChangelogHelper changelogHelper;
    private final NotificationService notificationService;
    private final WatcherService watcherService;

    @Autowired
    public ScriptRepositoryImpl(
        @ComponentImport I18nHelper i18nHelper,
        @ComponentImport ClusterLockService clusterLockService,
        @ComponentImport ActiveObjects ao,
        ScriptInvalidationService scriptInvalidationService,
        ScriptService scriptService,
        JsonMapper jsonMapper,
        AuditLogRepository auditLogRepository,
        ExecutionRepository executionRepository,
        ChangelogHelper changelogHelper,
        NotificationService notificationService,
        WatcherService watcherService
    ) {
        this.i18nHelper = i18nHelper;
        this.clusterLockService = clusterLockService;
        this.ao = ao;
        this.scriptInvalidationService = scriptInvalidationService;
        this.scriptService = scriptService;
        this.jsonMapper = jsonMapper;
        this.auditLogRepository = auditLogRepository;
        this.executionRepository = executionRepository;
        this.changelogHelper = changelogHelper;
        this.notificationService = notificationService;
        this.watcherService = watcherService;
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
        for (RegistryScriptDto scriptDto : getAllScripts(true, true)) {
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

        addAuditLogAndNotify(user, EntityAction.CREATED, directory, directory.getID() + " - " + directory.getName());

        return buildDirectoryDto(directory);
    }

    @Override
    public ScriptDirectoryDto updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm form) {
        validateDirectoryForm(form);

        ScriptDirectory directory = ao.get(ScriptDirectory.class, id);
        directory.setName(form.getName());
        directory.setParent(getParentDirectory(form.getParentId()));
        directory.save();

        addAuditLogAndNotify(user, EntityAction.UPDATED, directory, directory.getID() + " - " + directory.getName());

        return buildDirectoryDto(directory);
    }

    @Override
    public void deleteDirectory(ApplicationUser user, int id) {
        deleteDirectory(user, ao.get(ScriptDirectory.class, id));
    }

    @Override
    public void restoreDirectory(ApplicationUser user, int id) {
        restoreDirectory(user, ao.get(ScriptDirectory.class, id));
    }

    private void deleteDirectory(ApplicationUser user, ScriptDirectory directory) {
        directory.setDeleted(true);
        directory.save();

        for (ScriptDirectory child : getChildren(directory)) {
            deleteDirectory(user, child);
        }

        for (Script script : getScripts(directory)) {
            deleteScript(user, script);
        }

        addAuditLogAndNotify(user, EntityAction.DELETED, directory, directory.getID() + " - " + directory.getName());
    }

    private void restoreDirectory(ApplicationUser user, ScriptDirectory directory) {
        directory.setDeleted(false);
        directory.save();

        ScriptDirectory parent = directory.getParent();
        if (parent != null && parent.isDeleted()) {
            restoreDirectory(user, parent);
        }

        addAuditLogAndNotify(user, EntityAction.RESTORED, directory, directory.getID() + " - " + directory.getName());
    }

    @Override
    public void moveDirectory(ApplicationUser user, int id, ParentForm form) {
        ScriptDirectory directory = ao.get(ScriptDirectory.class, id);
        ScriptDirectory oldParent = directory.getParent();
        ScriptDirectory newParent = null;

        if (form.getParentId() != null) {
            newParent = ao.get(ScriptDirectory.class, form.getParentId());
        }

        directory.setParent(newParent);
        directory.save();

        addAuditLogAndNotify(user, EntityAction.MOVED, directory, getName(oldParent) + " -> " + getName(newParent));
    }

    @Override
    public List<RegistryScriptDto> getAllScripts(boolean includeChangelog, boolean includeErrorCount) {
        return Arrays
            .stream(ao.find(Script.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(script -> buildScriptDto(script, includeChangelog, false, includeErrorCount))
            .collect(Collectors.toList());
    }

    @Override
    public List<ScriptDescription> getAllScriptDescriptions(WorkflowScriptType type) {
        return Arrays
            .stream(ao.find(Script.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(this::buildScriptDescription)
            .filter(description -> description.getTypes().contains(type))
            .sorted(Comparator.comparing(ScriptDescription::getName))
            .collect(Collectors.toList());
    }

    //todo: cache result, maybe add new method for workflow functions
    @Override
    public RegistryScriptDto getScript(int id, boolean includeChangelogs, boolean expandName, boolean includeErrorCount) {
        return buildScriptDto(ao.get(Script.class, id), includeChangelogs, expandName, includeErrorCount);
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
            new DBParam("DESCRIPTION", scriptForm.getDescription()),
            new DBParam("SCRIPT_BODY", scriptForm.getScriptBody()),
            new DBParam("DIRECTORY_ID", scriptForm.getDirectoryId()),
            new DBParam("DELETED", false),
            new DBParam("PARAMETERS", parameters),
            new DBParam("TYPES", scriptForm.getTypes().stream().map(WorkflowScriptType::name).collect(Collectors.joining(",")))
        );

        String diff = changelogHelper.generateDiff(script.getID(), "", script.getName(), "", scriptForm.getScriptBody());

        String comment = scriptForm.getComment();
        if (comment == null) {
            comment = "Created.";
        }

        changelogHelper.addChangelog(Changelog.class, script.getID(), user.getKey(), diff, comment);

        addAuditLogAndNotify(user, EntityAction.CREATED, script, diff, comment);

        return buildScriptDto(script, true, false, true);
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

    @Override
    public void moveScript(ApplicationUser user, int id, ParentForm form) {
        Script script = ao.get(Script.class, id);
        ScriptDirectory oldParent = script.getDirectory();
        ScriptDirectory newParent = null;

        if (form.getParentId() != null) {
            newParent = ao.get(ScriptDirectory.class, form.getParentId());
        }

        script.setDirectory(newParent);
        script.save();

        addAuditLogAndNotify(user, EntityAction.MOVED, script, null, getName(oldParent) + " -> " + getName(newParent));
    }

    private RegistryScriptDto doUpdateScript(ApplicationUser user, int id, RegistryScriptForm form, ParseContext parseContext) {
        Script script = ao.get(Script.class, id);

        if (script.isDeleted()) {
            throw new IllegalArgumentException("Script " + id + " is deleted");
        }

        String diff = changelogHelper.generateDiff(id, script.getName(), form.getName(), script.getScriptBody(), form.getScriptBody());
        String comment = form.getComment();

        changelogHelper.addChangelog(Changelog.class, script.getID(), user.getKey(), diff, comment);

        String parameters = parseContext.getParameters().size() > 0 ? jsonMapper.write(parseContext.getParameters()) : null;

        script.setName(form.getName());
        script.setDescription(form.getDescription());
        script.setScriptBody(form.getScriptBody());
        script.setParameters(parameters);
        script.setTypes(form.getTypes().stream().map(WorkflowScriptType::name).collect(Collectors.joining(",")));
        script.save();

        addAuditLogAndNotify(user, EntityAction.UPDATED, script, diff, comment);

        return buildScriptDto(script, true, false, true);
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

    @Override
    public void restoreScript(ApplicationUser user, int id) {
        ClusterLock lock = clusterLockService.getLockForName(getLockKey(id));

        lock.lock();
        try {
            restoreScript(user, ao.get(Script.class, id));
        } finally {
            lock.unlock();
        }
    }

    private void deleteScript(ApplicationUser user, Script script) {
        script.setDeleted(true);
        script.save();

        addAuditLogAndNotify(user, EntityAction.DELETED, script, null, script.getID() + " - " + script.getName());
    }

    private void restoreScript(ApplicationUser user, Script script) {
        script.setDeleted(false);
        script.save();

        restoreDirectory(user, script.getDirectory());

        addAuditLogAndNotify(user, EntityAction.DELETED, script, null, script.getID() + " - " + script.getName());
    }

    private void addAuditLogAndNotify(ApplicationUser user, EntityAction action, ScriptDirectory directory, String description) {
        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                EntityType.REGISTRY_DIRECTORY,
                directory.getID(),
                action,
                description
            )
        );

        notificationService.sendNotifications(
            new NotificationDto(
                user, action, EntityType.REGISTRY_DIRECTORY, directory.getName(), directory.getID(), null, null, description
            ),
            getWatchers(directory)
        );
    }

    private void addAuditLogAndNotify(ApplicationUser user, EntityAction action, Script script, String diff, String description) {
        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                EntityType.REGISTRY_SCRIPT,
                script.getID(),
                action,
                description
            )
        );

        notificationService.sendNotifications(
            new NotificationDto(
                user, action, EntityType.REGISTRY_SCRIPT, script.getName(), script.getID(), diff, null, description
            ),
            getWatchers(script)
        );
    }

    private RegistryScriptDto buildScriptDto(Script script, boolean includeChangelogs, boolean expandName, boolean includeErrorCount) {
        RegistryScriptDto result = new RegistryScriptDto();

        result.setId(script.getID());
        result.setDescription(script.getDescription());
        result.setDirectoryId(script.getDirectory().getID());
        result.setScriptBody(script.getScriptBody());
        result.setDeleted(script.isDeleted());
        result.setParentName(ScriptUtil.getExpandedName(script.getDirectory()));

        if (expandName) {
            result.setName(ScriptUtil.getExpandedName(script));
        } else {
            result.setName(script.getName());
        }
        if (includeChangelogs) {
            result.setChangelogs(changelogHelper.collect(script.getChangelogs()));
        }
        if (includeErrorCount) {
            result.setErrorCount(executionRepository.getErrorCount(script.getID()));
        }

        if (script.getParameters() != null) {
            result.setParams(jsonMapper.read(script.getParameters(), Const.PARAM_LIST_TYPE_REF));
        }

        result.setTypes(parseTypes(script.getTypes()));

        return result;
    }

    private static Set<WorkflowScriptType> parseTypes(String types) {
        if (types == null) {
            return EnumSet.allOf(WorkflowScriptType.class);
        } else {
            return Arrays.stream(types.split(",")).map(WorkflowScriptType::valueOf).collect(Collectors.toSet());
        }
    }

    private static ScriptDirectoryDto buildDirectoryDto(ScriptDirectory directory) {
        ScriptDirectoryDto result = new ScriptDirectoryDto();

        result.setId(directory.getID());
        result.setName(directory.getName());
        result.setFullName(ScriptUtil.getExpandedName(directory));

        ScriptDirectory parent = directory.getParent();
        if (parent != null) {
            result.setParentId(parent.getID());
        }

        return result;
    }

    private ScriptDirectoryTreeDto buildDirectoryTreeDto(ScriptDirectory directory, Multimap<Integer, RegistryScriptDto> scripts) {
        ScriptDirectoryTreeDto result = new ScriptDirectoryTreeDto();

        result.setId(directory.getID());
        result.setName(directory.getName());
        result.setChildren(
            Arrays
                .stream(getChildren(directory))
                .map(child -> buildDirectoryTreeDto(child, scripts))
                .sorted(Comparator.comparing(ScriptDirectoryTreeDto::getName))
                .collect(Collectors.toList())
        );
        result.setScripts(
            scripts
                .get(directory.getID())
                .stream()
                .sorted(Comparator.comparing(RegistryScriptDto::getName))
                .collect(Collectors.toList())
        );

        return result;
    }

    private void validateDirectoryForm(ScriptDirectoryForm form) {
        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        if (form.getName().length() > 32) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "name");
        }
    }

    private ParseContext validateScriptForm(boolean isNew, RegistryScriptForm form) {
        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        String description = StringUtils.trimToNull(form.getDescription());
        form.setDescription(description);
        if (description != null) {
            if (form.getDescription().length() > 10000) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "description");
            }
        }

        if (form.getName().length() > 64) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "name");
        }

        if (StringUtils.isEmpty(form.getScriptBody())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "scriptBody");
        }
        ParseContext parseContext = scriptService.parseScript(form.getScriptBody());

        String comment = StringUtils.trimToNull(form.getComment());
        form.setComment(comment);

        if (!isNew) {
            if (StringUtils.isEmpty(comment)) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }
        }

        if (comment != null) {
            if (comment.length() > Const.COMMENT_MAX_LENGTH) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "comment");
            }
        }

        if (form.getTypes() == null || form.getTypes().size() == 0) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "types");
        }

        return parseContext;
    }

    private ScriptDescription buildScriptDescription(Script script) {
        ScriptDescription result = new ScriptDescription();
        result.setId(script.getID());
        result.setName(ScriptUtil.getExpandedName(script));
        result.setTypes(parseTypes(script.getTypes()));

        if (script.getParameters() != null) {
            result.setParams(jsonMapper.read(script.getParameters(), Const.PARAM_LIST_TYPE_REF));
        }

        return result;
    }

    private List<ApplicationUser> getWatchers(Script script) {
        List<ApplicationUser> watchers = getWatchers(script.getDirectory());

        watchers.addAll(watcherService.getWatchers(EntityType.REGISTRY_SCRIPT, script.getID()));

        return watchers;
    }

    private List<ApplicationUser> getWatchers(ScriptDirectory directory) {
        List<ApplicationUser> watchers = new ArrayList<>();

        while (directory != null) {
            watchers.addAll(watcherService.getWatchers(EntityType.REGISTRY_DIRECTORY, directory.getID()));

            directory = directory.getParent();
        }

        return watchers;
    }

    private ScriptDirectory[] getChildren(ScriptDirectory directory) {
        return ao
            .find(
                ScriptDirectory.class,
                Query.select().where("DELETED = ? AND PARENT_ID = ?", Boolean.FALSE, directory.getID())
            );
    }

    private Script[] getScripts(ScriptDirectory directory) {
        return ao
            .find(
                Script.class,
                Query.select().where("DELETED = ? AND DIRECTORY_ID = ?", Boolean.FALSE, directory.getID())
            );
    }

    private static String getLockKey(int id) {
        return ScriptRepositoryImpl.class.toString() + "_script_" + id;
    }

    private static String getName(ScriptDirectory directory) {
        return directory != null ? (directory.getName() + "(" + directory.getID() + ")") : null;
    }
}
