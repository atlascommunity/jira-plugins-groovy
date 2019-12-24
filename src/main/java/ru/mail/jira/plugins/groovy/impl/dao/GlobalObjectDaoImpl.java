package ru.mail.jira.plugins.groovy.impl.dao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dao.GlobalObjectDao;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.impl.AuditService;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@ExportAsDevService(GlobalObjectDao.class)
public class GlobalObjectDaoImpl implements GlobalObjectDao {
    private final ActiveObjects ao;
    private final ChangelogHelper changelogHelper;
    private final AuditService auditService;

    @Autowired
    public GlobalObjectDaoImpl(
        @ComponentImport ActiveObjects ao,
        ChangelogHelper changelogHelper,
        AuditService auditService
    ) {
        this.ao = ao;
        this.changelogHelper = changelogHelper;
        this.auditService = auditService;
    }

    @Override
    public List<GlobalObject> getAll() {
        return Arrays.asList(ao.find(GlobalObject.class, Query.select().where("DELETED = ?", Boolean.FALSE)));
    }

    @Override
    public GlobalObjectChangelog[] getChangelogs(int id) {
        return ao.find(GlobalObjectChangelog.class, Query.select().where("SCRIPT_ID = ?", id));
    }

    @Override
    public GlobalObject get(int id) {
        return ao.get(GlobalObject.class, id);
    }

    @Override
    public GlobalObject getByName(String name) {
        GlobalObject[] objects = ao.find(GlobalObject.class, Query.select().where("LOWER_NAME = ?", name.toLowerCase()));
        return objects.length > 0 ? objects[0] : null;
    }

    @Override
    public GlobalObject createScript(ApplicationUser user, GlobalObjectForm form) {
        GlobalObject result = ao.create(
            GlobalObject.class,
            new DBParam("UUID", UUID.randomUUID().toString()),
            new DBParam("NAME", form.getName()),
            new DBParam("LOWER_NAME", form.getName().toLowerCase()),
            new DBParam("DESCRIPTION", form.getDescription()),
            new DBParam("SCRIPT_BODY", form.getScriptBody()),
            new DBParam("DELETED", false),
            new DBParam("DEPENDENCIES", form.getDependencies())
        );

        String diff = changelogHelper.generateDiff(result.getID(), "", result.getName(), "", form.getScriptBody());

        String comment = form.getComment();
        if (comment == null) {
            comment = Const.CREATED_COMMENT;
        }

        changelogHelper.addChangelog(GlobalObjectChangelog.class, "SCRIPT_ID", result.getID(), null, user.getKey(), diff, comment);

        addAuditLogAndNotify(user, EntityAction.CREATED, result, diff, comment);

        return result;
    }

    @Override
    public GlobalObject updateScript(ApplicationUser user, int id, GlobalObjectForm form) {
        GlobalObject result = get(id);

        if (result == null || result.isDeleted()) {
            throw new RuntimeException("Object is deleted");
        }

        String diff = changelogHelper.generateDiff(id, result.getName(), form.getName(), result.getScriptBody(), form.getScriptBody());
        String comment = form.getComment();

        changelogHelper.addChangelog(GlobalObjectChangelog.class, "SCRIPT_ID", result.getID(), result.getUuid(), user.getKey(), diff, comment);

        result.setUuid(UUID.randomUUID().toString());
        result.setName(form.getName());
        result.setLowerName(form.getName().toLowerCase());
        result.setDescription(form.getDescription());
        result.setScriptBody(form.getScriptBody());
        result.setDependencies(form.getDependencies());
        result.save();

        addAuditLogAndNotify(user, EntityAction.UPDATED, result, diff, comment);

        return result;
    }

    @Override
    public void deleteScript(ApplicationUser user, int id) {
        GlobalObject object = get(id);
        object.setDeleted(true);
        object.save();

        addAuditLogAndNotify(user, EntityAction.DELETED, object, null, object.getID() + " - " + object.getName());
    }

    @Override
    public void restoreScript(ApplicationUser user, int id) {
        GlobalObject object = get(id);
        object.setDeleted(false);
        object.save();

        addAuditLogAndNotify(user, EntityAction.RESTORED, object, null, object.getID() + " - " + object.getName());
    }

    private void addAuditLogAndNotify(ApplicationUser user, EntityAction action, GlobalObject object, String diff, String description) {
        auditService.addAuditLogAndNotify(user, action, EntityType.GLOBAL_OBJECT, object, diff, description);
    }
}
