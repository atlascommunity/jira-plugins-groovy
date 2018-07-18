package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.notification.NotificationDto;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.service.NotificationService;
import ru.mail.jira.plugins.groovy.api.service.WatcherService;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.TypeUtil;
import ru.mail.jira.plugins.groovy.util.CustomFieldHelper;
import ru.mail.jira.plugins.groovy.util.RestFieldException;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigDto;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.impl.cf.ScriptedCFType;
import ru.mail.jira.plugins.groovy.impl.cf.TemplateScriptedCFType;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.Const;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FieldConfigRepositoryImpl implements FieldConfigRepository {
    private final Logger logger = LoggerFactory.getLogger(FieldConfigRepositoryImpl.class);

    private final ActiveObjects ao;
    private final I18nHelper i18nHelper;
    private final FieldConfigManager fieldConfigManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final CustomFieldManager customFieldManager;
    private final ScriptService scriptService;
    private final ChangelogHelper changelogHelper;
    private final Cache<Long, FieldScript> scriptCache;
    private final ScriptInvalidationService invalidationService;
    private final ExecutionRepository executionRepository;
    private final CustomFieldHelper customFieldHelper;
    private final WatcherService watcherService;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Autowired
    public FieldConfigRepositoryImpl(
        @ComponentImport ActiveObjects ao,
        @ComponentImport I18nHelper i18nHelper,
        @ComponentImport FieldConfigManager fieldConfigManager,
        @ComponentImport FieldConfigSchemeManager fieldConfigSchemeManager,
        @ComponentImport CustomFieldManager customFieldManager,
        @ComponentImport CacheManager cacheManager,
        ScriptService scriptService,
        ChangelogHelper changelogHelper,
        ScriptInvalidationService invalidationService,
        ExecutionRepository executionRepository,
        CustomFieldHelper customFieldHelper,
        WatcherService watcherService,
        AuditLogRepository auditLogRepository,
        NotificationService notificationService
    ) {
        this.ao = ao;
        this.i18nHelper = i18nHelper;
        this.fieldConfigManager = fieldConfigManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.customFieldManager = customFieldManager;
        this.scriptService = scriptService;
        this.changelogHelper = changelogHelper;
        this.invalidationService = invalidationService;
        this.executionRepository = executionRepository;
        this.customFieldHelper = customFieldHelper;
        this.watcherService = watcherService;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;

        this.scriptCache = cacheManager
            .getCache(
                FieldConfigRepositoryImpl.class.getCanonicalName() + ".cache",
                this::doGetScript,
                new CacheSettingsBuilder()
                    .remote()
                    .replicateViaInvalidation()
                    .build()
            );
    }

    @Override
    public List<FieldConfigDto> getAllConfigs() {
        return customFieldManager
            .getCustomFieldObjects()
            .stream()
            .filter(field -> field.getCustomFieldType() instanceof ScriptedCFType)
            .flatMap(this::getConfigs)
            .distinct()
            .collect(Collectors.toList());
    }

    private Stream<FieldConfigDto> getConfigs(CustomField customField) {
        return customField
            .getConfigurationSchemes()
            .stream()
            .flatMap(fieldConfigScheme -> fieldConfigScheme.getConfigs().values().stream())
            .map(config -> buildDto(config, findConfig(config.getId()), true, true));
    }

    @Override
    public FieldConfigDto getConfig(long id, boolean includeChangelogs) {
        com.atlassian.jira.issue.fields.config.FieldConfig jiraFieldConfig = fieldConfigManager.getFieldConfig(id);
        return buildDto(jiraFieldConfig, findConfig(id), includeChangelogs, true);
    }

    @Override
    public FieldConfigDto updateConfig(ApplicationUser user, long configId, FieldConfigForm form) {
        FieldConfig fieldConfig = findConfig(configId);
        com.atlassian.jira.issue.fields.config.FieldConfig jiraFieldConfig = fieldConfigManager.getFieldConfig(configId);

        if (jiraFieldConfig == null) {
            throw new IllegalArgumentException("Coudn't find field config with id " + configId);
        }

        boolean isTemplated = isTemplated(jiraFieldConfig);

        EntityAction action;
        String comment;
        String diff;
        String templateDiff = null;

        if (fieldConfig == null) {
            validate(true, isTemplated, form);

            fieldConfig = ao.create(
                FieldConfig.class,
                new DBParam("FIELD_CONFIG_ID", configId),
                new DBParam("UUID", UUID.randomUUID().toString()),
                new DBParam("SCRIPT_BODY", form.getScriptBody()),
                new DBParam("CACHEABLE", form.isCacheable()),
                new DBParam("TEMPLATE", form.getTemplate()),
                new DBParam("VELOCITY_PARAMS_ENABLED", form.isVelocityParamsEnabled()),
                new DBParam("COMPILE_STATIC", form.isCompileStatic())
            );

            diff = changelogHelper.generateDiff(configId, "", "field", "", form.getScriptBody());

            Map<String, Object> additionalParams = new HashMap<>();
            if (isTemplated) {
                templateDiff = changelogHelper.generateDiff(configId, "", "field", "", form.getTemplate());
                additionalParams.put("TEMPLATE_DIFF", StringUtils.isEmpty(templateDiff) ? "no changes" : templateDiff);
            }

            comment = form.getComment();
            if (comment == null) {
                comment = "Created.";
            }

            changelogHelper.addChangelog(FieldConfigChangelog.class, "FIELD_CONFIG_ID", fieldConfig.getID(), user.getKey(), diff, comment, additionalParams);

            action = EntityAction.CREATED;
        } else {
            validate(false, isTemplated, form);

            diff = changelogHelper.generateDiff(configId, "field", "field", fieldConfig.getScriptBody(), form.getScriptBody());

            Map<String, Object> additionalParams = new HashMap<>();
            if (isTemplated) {
                templateDiff = changelogHelper.generateDiff(configId, "field", "field", fieldConfig.getTemplate(), form.getTemplate());
                additionalParams.put("TEMPLATE_DIFF", StringUtils.isEmpty(templateDiff) ? "no changes" : templateDiff);
            }

            comment = form.getComment();
            changelogHelper.addChangelog(FieldConfigChangelog.class, "FIELD_CONFIG_ID", fieldConfig.getID(), user.getKey(), diff, comment, additionalParams);

            fieldConfig.setCacheable(form.isCacheable());
            fieldConfig.setCompileStatic(form.isCompileStatic());
            fieldConfig.setScriptBody(form.getScriptBody());
            fieldConfig.setUuid(UUID.randomUUID().toString());
            fieldConfig.setTemplate(form.getTemplate());
            fieldConfig.setVelocityParamsEnabled(form.isVelocityParamsEnabled());
            fieldConfig.save();

            action = EntityAction.UPDATED;
        }

        CustomField cf = jiraFieldConfig.getCustomField();

        addAuditLogAndNotify(user, action, fieldConfig, (int) configId, cf != null ? cf.getName() : "undefined", diff, templateDiff, comment);

        scriptCache.remove(configId);
        if (cf != null) {
            invalidationService.invalidateField(cf.getIdAsLong());
        } else {
            logger.error("CustomField is null for field config {}", jiraFieldConfig.getId());
        }
        return buildDto(jiraFieldConfig, fieldConfig, true, true);
    }

    @Override
    public FieldScript getScript(long fieldConfigId) {
        return scriptCache.get(fieldConfigId);
    }

    @Override
    public void invalidateAll() {
        scriptCache.removeAll();
    }

    @Nonnull
    private FieldScript doGetScript(long fieldConfigId) {
        FieldConfig fieldConfig = findConfig(fieldConfigId);

        if (fieldConfig == null) {
            return new FieldScript();
        }

        return new FieldScript(
            fieldConfig.getUuid(),
            fieldConfig.getScriptBody(),
            fieldConfig.getTemplate(),
            fieldConfig.getCacheable(),
            fieldConfig.getCompileStatic() != null ? fieldConfig.getCompileStatic() : false,
            fieldConfig.isVelocityParamsEnabled()
        );
    }

    private void addAuditLogAndNotify(
        ApplicationUser user, EntityAction action,
        FieldConfig fieldConfig, int jiraFieldConfigId,
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
                fieldConfig.getID(),
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

    private void validate(boolean isNew, boolean template, FieldConfigForm form) {
        if (form.isCompileStatic()) {
            scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getFieldConfigTypes(form.isVelocityParamsEnabled()));
        } else {
            scriptService.parseScript(form.getScriptBody());
        }

        if (StringUtils.isEmpty(form.getScriptBody())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "scriptBody");
        }

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

        if (template) {
            if (StringUtils.isEmpty(form.getTemplate())) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "template");
            }
        } else {
            form.setTemplate("");
        }
    }

    private FieldConfigDto buildDto(com.atlassian.jira.issue.fields.config.FieldConfig jiraConfig, FieldConfig fieldConfig, boolean includeChangelogs, boolean includeErrorCount) {
        FieldConfigDto result = new FieldConfigDto();

        CustomField customField = jiraConfig.getCustomField();

        boolean isTemplated = isTemplated(jiraConfig);

        result.setId(jiraConfig.getId());

        String customFieldName = customField.getName();
        String contextName = fieldConfigSchemeManager.getConfigSchemeForFieldConfig(jiraConfig).getName();

        result.setName(customFieldName + " - " + contextName);
        result.setCustomFieldName(customFieldName);
        result.setCustomFieldId(customField.getIdAsLong());
        result.setContextName(contextName);

        result.setNeedsTemplate(isTemplated);
        result.setType(customField.getCustomFieldType().getName());

        CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
        if (searcher != null) {
            result.setSearcher(searcher.getDescriptor().getName());
        }
        result.setExpectedType(customFieldHelper.getExpectedType(customField).getCanonicalName());

        if (fieldConfig == null) {
            result.setCacheable(true);
            result.setCompileStatic(true);
            result.setScriptBody("");
            if (isTemplated) {
                result.setTemplate("");
            }
            result.setChangelogs(ImmutableList.of());
        } else {
            result.setCacheable(fieldConfig.getCacheable());
            result.setCompileStatic(fieldConfig.getCompileStatic() != null ? fieldConfig.getCompileStatic() : false);
            result.setVelocityParamsEnabled(fieldConfig.isVelocityParamsEnabled());
            result.setScriptBody(fieldConfig.getScriptBody());
            result.setUuid(fieldConfig.getUuid());

            if (isTemplated) {
                result.setTemplate(fieldConfig.getTemplate());
            }

            if (includeChangelogs) {
                result.setChangelogs(changelogHelper.collect(fieldConfig.getChangelogs()));
            }

            if (includeErrorCount) {
                result.setErrorCount(executionRepository.getErrorCount(fieldConfig.getUuid()));
                result.setWarningCount(executionRepository.getWarningCount(fieldConfig.getUuid()));
            }
        }

        return result;
    }

    private FieldConfig findConfig(long fieldConfigId) {
        FieldConfig[] fieldConfigs = ao.find(FieldConfig.class, Query.select().where("FIELD_CONFIG_ID = ?", fieldConfigId));

        return fieldConfigs.length > 0 ? fieldConfigs[0] : null;
    }

    private boolean isTemplated(com.atlassian.jira.issue.fields.config.FieldConfig jiraFieldConfig) {
        return TemplateScriptedCFType.class.equals(jiraFieldConfig.getCustomField().getCustomFieldType().getClass());
    }
}
