package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.jira.issue.CustomFieldManager;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.entity.AuditCategory;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigDto;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.api.entity.AuditAction;
import ru.mail.jira.plugins.groovy.api.entity.FieldConfig;
import ru.mail.jira.plugins.groovy.api.entity.FieldConfigChangelog;
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
    private final ActiveObjects ao;
    private final I18nHelper i18nHelper;
    private final FieldConfigManager fieldConfigManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final CustomFieldManager customFieldManager;
    private final ScriptService scriptService;
    private final ChangelogHelper changelogHelper;
    private final AuditLogRepository auditLogRepository;
    private final Cache<Long, FieldScript> scriptCache;
    private final ScriptInvalidationService invalidationService;
    private final ExecutionRepository executionRepository;

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
        AuditLogRepository auditLogRepository,
        ScriptInvalidationService invalidationService,
        ExecutionRepository executionRepository
    ) {
        this.ao = ao;
        this.i18nHelper = i18nHelper;
        this.fieldConfigManager = fieldConfigManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.customFieldManager = customFieldManager;
        this.scriptService = scriptService;
        this.changelogHelper = changelogHelper;
        this.scriptCache = cacheManager
            .getCache(
                FieldConfigRepositoryImpl.class.getCanonicalName() + ".cache",
                this::doGetScript,
                new CacheSettingsBuilder()
                    .remote()
                    .replicateViaInvalidation()
                    .build()
            );
        this.auditLogRepository = auditLogRepository;
        this.invalidationService = invalidationService;
        this.executionRepository = executionRepository;
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

        AuditAction action;

        if (fieldConfig == null) {
            validate(true, isTemplated, form);

            fieldConfig = ao.create(
                FieldConfig.class,
                new DBParam("FIELD_CONFIG_ID", configId),
                new DBParam("UUID", UUID.randomUUID().toString()),
                new DBParam("SCRIPT_BODY", form.getScriptBody()),
                new DBParam("CACHEABLE", form.isCacheable()),
                new DBParam("TEMPLATE", form.getTemplate()),
                new DBParam("VELOCITY_PARAMS_ENABLED", form.isVelocityParamsEnabled())
            );

            String diff = changelogHelper.generateDiff(configId, "", "field", "", form.getScriptBody());

            Map<String, Object> additionalParams = new HashMap<>();
            if (isTemplated) {
                String templateDiff = changelogHelper.generateDiff(configId, "", "field", "", form.getTemplate());
                additionalParams.put("TEMPLATE_DIFF", StringUtils.isEmpty(templateDiff) ? "no changes" : templateDiff);
            }

            changelogHelper.addChangelog(FieldConfigChangelog.class, "FIELD_CONFIG_ID", fieldConfig.getID(), user.getKey(), diff, "Created.", additionalParams);

            action = AuditAction.CREATED;
        } else {
            validate(false, isTemplated, form);

            String diff = changelogHelper.generateDiff(configId, "field", "field", fieldConfig.getScriptBody(), form.getScriptBody());

            Map<String, Object> additionalParams = new HashMap<>();
            if (isTemplated) {
                String templateDiff = changelogHelper.generateDiff(configId, "field", "field", fieldConfig.getTemplate(), form.getTemplate());
                additionalParams.put("TEMPLATE_DIFF", StringUtils.isEmpty(templateDiff) ? "no changes" : templateDiff);
            }

            changelogHelper.addChangelog(FieldConfigChangelog.class, "FIELD_CONFIG_ID", fieldConfig.getID(), user.getKey(), diff, form.getComment(), additionalParams);

            fieldConfig.setCacheable(form.isCacheable());
            fieldConfig.setScriptBody(form.getScriptBody());
            fieldConfig.setUuid(UUID.randomUUID().toString());
            fieldConfig.setTemplate(form.getTemplate());
            fieldConfig.setVelocityParamsEnabled(form.isVelocityParamsEnabled());
            fieldConfig.save();

            action = AuditAction.UPDATED;
        }

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.CUSTOM_FIELD,
                action,
                String.valueOf(fieldConfig.getID())
            )
        );

        scriptCache.remove(configId);
        invalidationService.invalidateField(jiraFieldConfig.getCustomField().getIdAsLong());
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
            fieldConfig.isVelocityParamsEnabled()
        );
    }

    private void validate(boolean isNew, boolean template, FieldConfigForm form) {
        scriptService.parseScript(form.getScriptBody());

        if (StringUtils.isEmpty(form.getScriptBody())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "scriptBody");
        }

        if (!isNew) {
            if (StringUtils.isEmpty(form.getComment())) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }

            if (form.getComment().length() > Const.COMMENT_MAX_LENGTH) {
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
        result.setCustomFieldName(customField.getName());
        result.setCustomFieldId(customField.getIdAsLong());
        result.setContextName(fieldConfigSchemeManager.getConfigSchemeForFieldConfig(jiraConfig).getName());
        result.setNeedsTemplate(isTemplated);

        if (fieldConfig == null) {
            result.setCacheable(true);
            result.setScriptBody("");
            if (isTemplated) {
                result.setTemplate("");
            }
            result.setChangelogs(ImmutableList.of());
        } else {
            result.setCacheable(fieldConfig.getCacheable());
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
