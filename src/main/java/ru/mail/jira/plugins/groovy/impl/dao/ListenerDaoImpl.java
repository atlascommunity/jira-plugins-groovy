package ru.mail.jira.plugins.groovy.impl.dao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dao.ListenerDao;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.entity.Listener;
import ru.mail.jira.plugins.groovy.api.entity.ListenerChangelog;
import ru.mail.jira.plugins.groovy.impl.AuditService;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.JsonMapper;

import java.util.UUID;

@Component
public class ListenerDaoImpl implements ListenerDao {
    private final ActiveObjects ao;
    private final ChangelogHelper changelogHelper;
    private final AuditService auditService;
    private final JsonMapper jsonMapper;

    @Autowired
    public ListenerDaoImpl(
        @ComponentImport ActiveObjects ao,
        ChangelogHelper changelogHelper,
        AuditService auditService,
        JsonMapper jsonMapper
    ) {
        this.ao = ao;
        this.changelogHelper = changelogHelper;
        this.auditService = auditService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Listener createEventListener(ApplicationUser user, EventListenerForm form) {
        Listener listener = ao.create(
            Listener.class,
            new DBParam("UUID", UUID.randomUUID().toString()),
            new DBParam("NAME", form.getName()),
            new DBParam("DESCRIPTION", form.getDescription()),
            new DBParam("SCRIPT_BODY", form.getScriptBody()),
            new DBParam("DELETED", false),
            new DBParam("CONDITION", jsonMapper.write(form.getCondition())),
            new DBParam("ALWAYS_TRACK", form.isAlwaysTrack())
        );

        String diff = changelogHelper.generateDiff(listener.getID(), "", listener.getName(), "", form.getScriptBody());

        String comment = form.getComment();
        if (comment == null) {
            comment = Const.CREATED_COMMENT;
        }

        changelogHelper.addChangelog(ListenerChangelog.class, "LISTENER_ID", listener.getID(), user.getKey(), diff, comment);

        addAuditLogAndNotify(user, EntityAction.CREATED, listener, diff, comment);

        return listener;
    }

    @Override
    public Listener updateEventListener(ApplicationUser user, int id, EventListenerForm form) {
        Listener listener = ao.get(Listener.class, id);

        if (listener == null || listener.isDeleted()) {
            throw new RuntimeException("Event listener is deleted");
        }

        String diff = changelogHelper.generateDiff(id, listener.getName(), form.getName(), listener.getScriptBody(), form.getScriptBody());
        String comment = form.getComment();

        changelogHelper.addChangelog(ListenerChangelog.class, "LISTENER_ID", listener.getID(), user.getKey(), diff, comment);

        listener.setName(form.getName());
        listener.setDescription(form.getDescription());
        listener.setUuid(UUID.randomUUID().toString());
        listener.setScriptBody(form.getScriptBody());
        listener.setCondition(jsonMapper.write(form.getCondition()));
        listener.setAlwaysTrack(form.isAlwaysTrack());
        listener.save();

        addAuditLogAndNotify(user, EntityAction.UPDATED, listener, diff, comment);

        return listener;
    }

    @Override
    public void deleteEventListener(ApplicationUser user, int id) {
        Listener listener = ao.get(Listener.class, id);
        listener.setDeleted(true);
        listener.save();

        addAuditLogAndNotify(user, EntityAction.DELETED, listener, null, listener.getID() + " - " + listener.getName());
    }

    @Override
    public void restoreEventListener(ApplicationUser user, int id) {
        Listener listener = ao.get(Listener.class, id);
        listener.setDeleted(false);
        listener.save();

        addAuditLogAndNotify(user, EntityAction.RESTORED, listener, null, listener.getID() + " - " + listener.getName());
    }

    private void addAuditLogAndNotify(ApplicationUser user, EntityAction action, Listener listener, String diff, String description) {
        auditService.addAuditLogAndNotify(user, action, EntityType.LISTENER, listener, diff, description);
    }
}
