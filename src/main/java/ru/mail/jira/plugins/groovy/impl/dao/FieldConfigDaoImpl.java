package ru.mail.jira.plugins.groovy.impl.dao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dao.FieldConfigDao;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.dto.notification.NotificationDto;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.entity.FieldScript;
import ru.mail.jira.plugins.groovy.api.entity.FieldConfigChangelog;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.service.NotificationService;
import ru.mail.jira.plugins.groovy.api.service.WatcherService;
import ru.mail.jira.plugins.groovy.impl.cf.TemplateScriptedCFType;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.Const;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class FieldConfigDaoImpl implements FieldConfigDao {
    private final ActiveObjects ao;
    private final NotificationService notificationService;
    private final AuditLogRepository auditLogRepository;
    private final ChangelogHelper changelogHelper;
    private final WatcherService watcherService;

    @Autowired
    public FieldConfigDaoImpl(
        @ComponentImport ActiveObjects ao,
        NotificationService notificationService,
        AuditLogRepository auditLogRepository,
        ChangelogHelper changelogHelper,
        WatcherService watcherService
    ) {
        this.ao = ao;
        this.notificationService = notificationService;
        this.auditLogRepository = auditLogRepository;
        this.changelogHelper = changelogHelper;
        this.watcherService = watcherService;
    }

    @Override
    public FieldScript findByConfigId(long id) {
        return findConfig(id);
    }

    @Override
    public FieldScript createConfig(ApplicationUser user, FieldConfig jiraFieldConfig, FieldConfigForm form) {
        long configId = jiraFieldConfig.getId();

        FieldScript fieldScript = ao.create(
            FieldScript.class,
            new DBParam("FIELD_CONFIG_ID", configId),
            new DBParam("UUID", UUID.randomUUID().toString()),
            new DBParam("SCRIPT_BODY", form.getScriptBody()),
            new DBParam("CACHEABLE", form.isCacheable()),
            new DBParam("TEMPLATE", form.getTemplate()),
            new DBParam("VELOCITY_PARAMS_ENABLED", form.isVelocityParamsEnabled())
        );

        String diff = changelogHelper.generateDiff(configId, "", "field", "", form.getScriptBody());
        String templateDiff = null;
        boolean isTemplated = isTemplated(jiraFieldConfig);

        Map<String, Object> additionalParams = new HashMap<>();
        if (isTemplated) {
            templateDiff = changelogHelper.generateDiff(configId, "", "field", "", form.getTemplate());
            additionalParams.put("TEMPLATE_DIFF", StringUtils.isEmpty(templateDiff) ? "no changes" : templateDiff);
        }

        String comment = form.getComment();
        if (comment == null) {
            comment = Const.CREATED_COMMENT;
        }

        changelogHelper.addChangelog(FieldConfigChangelog.class, "FIELD_CONFIG_ID", fieldScript.getID(), user.getKey(), diff, comment, additionalParams);

        CustomField cf = jiraFieldConfig.getCustomField();
        addAuditLogAndNotify(user, EntityAction.CREATED, fieldScript, (int) configId, cf != null ? cf.getName() : "undefined", diff, templateDiff, comment);

        return fieldScript;
    }

    @Override
    public FieldScript updateConfig(ApplicationUser user, int id, FieldConfig jiraFieldConfig, FieldConfigForm form) {
        long configId = jiraFieldConfig.getId();

        FieldScript fieldScript = ao.get(FieldScript.class, id);

        String diff = changelogHelper.generateDiff(configId, "field", "field", fieldScript.getScriptBody(), form.getScriptBody());
        String templateDiff = null;
        boolean isTemplated = isTemplated(jiraFieldConfig);

        Map<String, Object> additionalParams = new HashMap<>();
        if (isTemplated) {
            templateDiff = changelogHelper.generateDiff(configId, "field", "field", fieldScript.getTemplate(), form.getTemplate());
            additionalParams.put("TEMPLATE_DIFF", StringUtils.isEmpty(templateDiff) ? "no changes" : templateDiff);
        }

        changelogHelper.addChangelog(FieldConfigChangelog.class, "FIELD_CONFIG_ID", fieldScript.getID(), user.getKey(), diff, form.getComment(), additionalParams);

        fieldScript.setCacheable(form.isCacheable());
        fieldScript.setScriptBody(form.getScriptBody());
        fieldScript.setUuid(UUID.randomUUID().toString());
        fieldScript.setTemplate(form.getTemplate());
        fieldScript.setVelocityParamsEnabled(form.isVelocityParamsEnabled());
        fieldScript.save();

        CustomField cf = jiraFieldConfig.getCustomField();
        addAuditLogAndNotify(user, EntityAction.UPDATED, fieldScript, (int) configId, cf != null ? cf.getName() : "undefined", diff, templateDiff, form.getComment());

        return fieldScript;
    }

    private void addAuditLogAndNotify(
        ApplicationUser user, EntityAction action,
        FieldScript fieldScript, int jiraFieldConfigId,
        String fieldName, String diff, String templateDiff, String description
    ) {
        //notifications are related to jira field config id, audit logs to AO config id
        if (action == EntityAction.CREATED) {
            watcherService.addWatcher(EntityType.CUSTOM_FIELD, jiraFieldConfigId, user);
        }

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                EntityType.CUSTOM_FIELD,
                fieldScript.getID(),
                action,
                description
            )
        );

        notificationService.sendNotifications(
            new NotificationDto(
                user, action, EntityType.CUSTOM_FIELD, fieldName, jiraFieldConfigId, diff, templateDiff, description
            ),
            watcherService.getWatchers(EntityType.CUSTOM_FIELD, jiraFieldConfigId)
        );
    }

    private FieldScript findConfig(long fieldConfigId) {
        FieldScript[] fieldScripts = ao.find(FieldScript.class, Query.select().where("FIELD_CONFIG_ID = ?", fieldConfigId));

        return fieldScripts.length > 0 ? fieldScripts[0] : null;
    }

    private boolean isTemplated(FieldConfig jiraFieldConfig) {
        return TemplateScriptedCFType.class.equals(jiraFieldConfig.getCustomField().getCustomFieldType().getClass());
    }
}
