package ru.mail.jira.plugins.groovy.impl.dao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dao.RegistryDao;
import ru.mail.jira.plugins.groovy.api.dto.directory.ParentForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.RegistryScriptForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.ScriptDirectoryForm;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.api.service.WatcherService;
import ru.mail.jira.plugins.groovy.impl.AuditService;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RegistryDaoImpl implements RegistryDao {
    private final ActiveObjects ao;
    private final I18nHelper i18nHelper;
    private final AuditService auditService;
    private final WatcherService watcherService;
    private final ChangelogHelper changelogHelper;

    @Autowired
    public RegistryDaoImpl(
        @ComponentImport ActiveObjects ao,
        @ComponentImport I18nHelper i18nHelper,
        AuditService auditService,
        WatcherService watcherService,
        ChangelogHelper changelogHelper
    ) {
        this.ao = ao;
        this.i18nHelper = i18nHelper;
        this.auditService = auditService;
        this.watcherService = watcherService;
        this.changelogHelper = changelogHelper;
    }

    @Override
    public ScriptDirectory createDirectory(ApplicationUser user, ScriptDirectoryForm form) {
        ScriptDirectory directory = ao.create(
            ScriptDirectory.class,
            new DBParam("NAME", form.getName()),
            new DBParam("PARENT_ID", getParentDirectory(form.getParentId())),
            new DBParam("DELETED", false)
        );

        addAuditLogAndNotify(user, EntityAction.CREATED, directory, directory.getID() + " - " + directory.getName());

        return directory;
    }

    @Override
    public ScriptDirectory updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm form) {
        ScriptDirectory directory = ao.get(ScriptDirectory.class, id);
        directory.setName(form.getName());
        directory.setParent(getParentDirectory(form.getParentId()));
        directory.save();

        addAuditLogAndNotify(user, EntityAction.UPDATED, directory, directory.getID() + " - " + directory.getName());

        return directory;
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
    public void deleteDirectory(ApplicationUser user, int id) {
        deleteDirectory(user, ao.get(ScriptDirectory.class, id));
    }

    @Override
    public void restoreDirectory(ApplicationUser user, int id) {
        restoreDirectory(user, ao.get(ScriptDirectory.class, id));
    }

    @Override
    public Script createScript(ApplicationUser user, RegistryScriptForm scriptForm, String parameters) {
        Script script = ao.create(
            Script.class,
            new DBParam("NAME", scriptForm.getName()),
            new DBParam("UUID", UUID.randomUUID().toString()),
            new DBParam("DESCRIPTION", scriptForm.getDescription()),
            new DBParam("SCRIPT_BODY", scriptForm.getScriptBody()),
            new DBParam("DIRECTORY_ID", scriptForm.getDirectoryId()),
            new DBParam("DELETED", false),
            new DBParam("PARAMETERS", parameters),
            new DBParam("TYPES", scriptForm.getTypes().stream().map(WorkflowScriptType::name).collect(Collectors.joining(",")))
        );

        String comment = scriptForm.getComment();
        if (comment == null) {
            comment = Const.CREATED_COMMENT;
        }

        String diff = changelogHelper.generateDiff(script.getID(), "", script.getName(), "", scriptForm.getScriptBody());

        changelogHelper.addChangelog(Changelog.class, script.getID(), user.getKey(), diff, comment);

        addAuditLogAndNotify(user, EntityAction.CREATED, script, diff, comment);

        return script;
    }

    @Override
    public Script updateScript(ApplicationUser user, int id, RegistryScriptForm form, String parameters) {
        Script script = ao.get(Script.class, id);

        if (script.isDeleted()) {
            throw new IllegalArgumentException("Script " + id + " is deleted");
        }

        String diff = changelogHelper.generateDiff(id, script.getName(), form.getName(), script.getScriptBody(), form.getScriptBody());
        String comment = form.getComment();

        changelogHelper.addChangelog(Changelog.class, script.getID(), user.getKey(), diff, comment);

        script.setName(form.getName());
        script.setUuid(UUID.randomUUID().toString());
        script.setDescription(form.getDescription());
        script.setScriptBody(form.getScriptBody());
        script.setParameters(parameters);
        script.setTypes(form.getTypes().stream().map(WorkflowScriptType::name).collect(Collectors.joining(",")));
        script.save();

        addAuditLogAndNotify(user, EntityAction.UPDATED, script, diff, comment);

        return script;
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

    @Override
    public void deleteScript(ApplicationUser user, int id) {
        deleteScript(user, ao.get(Script.class, id));
    }

    @Override
    public void restoreScript(ApplicationUser user, int id) {
        restoreScript(user, ao.get(Script.class, id));
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

    private void deleteScript(ApplicationUser user, Script script) {
        script.setDeleted(true);
        script.save();

        addAuditLogAndNotify(user, EntityAction.DELETED, script, null, script.getID() + " - " + script.getName());
    }

    private void restoreScript(ApplicationUser user, Script script) {
        script.setDeleted(false);
        script.save();

        restoreDirectory(user, script.getDirectory());

        addAuditLogAndNotify(user, EntityAction.RESTORED, script, null, script.getID() + " - " + script.getName());
    }

    private void addAuditLogAndNotify(ApplicationUser user, EntityAction action, ScriptDirectory directory, String description) {
        auditService.addAuditLogAndNotify(
            user, action,
            EntityType.REGISTRY_DIRECTORY, directory.getID(), directory.getName(),
            null, null, description, getWatchers(directory)
        );
    }

    private void addAuditLogAndNotify(ApplicationUser user, EntityAction action, Script script, String diff, String description) {
        auditService.addAuditLogAndNotify(user, action, EntityType.REGISTRY_SCRIPT, script, diff, description, getWatchers(script));
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

    private static String getName(ScriptDirectory directory) {
        return directory != null ? (directory.getName() + "(" + directory.getID() + ")") : null;
    }
}
