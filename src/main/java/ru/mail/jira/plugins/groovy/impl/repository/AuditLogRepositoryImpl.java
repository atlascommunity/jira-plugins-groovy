package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryDto;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.Page;
import ru.mail.jira.plugins.groovy.util.CustomFieldHelper;
import ru.mail.jira.plugins.groovy.util.ScriptUtil;
import ru.mail.jira.plugins.groovy.util.UserMapper;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final ActiveObjects activeObjects;
    private final DateTimeFormatter dateTimeFormatter;
    private final CustomFieldHelper customFieldHelper;
    private final UserMapper userMapper;

    @Autowired
    public AuditLogRepositoryImpl(
        @ComponentImport ActiveObjects activeObjects,
        @ComponentImport DateTimeFormatter dateTimeFormatter,
        CustomFieldHelper customFieldHelper,
        UserMapper userMapper
    ) {
        this.activeObjects = activeObjects;
        this.dateTimeFormatter = dateTimeFormatter;
        this.customFieldHelper = customFieldHelper;
        this.userMapper = userMapper;
    }

    @Override
    public void create(ApplicationUser user, AuditLogEntryForm form) {
        activeObjects.create(
            AuditLogEntry.class,
            new DBParam("DATE", new Timestamp(System.currentTimeMillis())),
            new DBParam("USER_KEY", user.getKey()),
            new DBParam("CATEGORY", form.getCategory()),
            new DBParam("ACTION", form.getAction()),
            new DBParam("DESCRIPTION", form.getDescription()),
            new DBParam("ENTITY_ID", form.getEntityId())
        );
    }

    @Override
    public Page<AuditLogEntryDto> getPagedEntries(int offset, int limit) {
        List<AuditLogEntryDto> results = Arrays
            .stream(
                activeObjects.find(
                    AuditLogEntry.class,
                    Query.select().order("ID DESC").limit(limit).offset(offset)
                )
            )
            .map(this::buildEntryDto)
            .collect(Collectors.toList());
        int totalCount = activeObjects.count(AuditLogEntry.class);

        return new Page<>(offset, limit, totalCount, results);
    }

    private AuditLogEntryDto buildEntryDto(AuditLogEntry entry) {
        AuditLogEntryDto result = new AuditLogEntryDto();

        result.setDate(dateTimeFormatter.forLoggedInUser().format(entry.getDate()));
        result.setId(entry.getID());
        result.setUser(userMapper.buildUser(entry.getUserKey()));
        result.setAction(entry.getAction());
        result.setCategory(entry.getCategory());
        result.setDescription(entry.getDescription());

        Integer entityId = entry.getEntityId();
        if (entityId != null) {
            String name = null;
            String parentName = null;
            Boolean deleted = null;
            switch (entry.getCategory()) {
                case ADMIN_SCRIPT: {
                    AdminScript script = activeObjects.get(AdminScript.class, entityId);
                    name = script.getName();
                    deleted = script.isDeleted();
                    break;
                }
                case REGISTRY_SCRIPT: {
                    Script script = activeObjects.get(Script.class, entityId);
                    name = script.getName();
                    parentName = ScriptUtil.getExpandedName(script.getDirectory());
                    deleted = script.isDeleted();
                    break;
                }
                case REGISTRY_DIRECTORY: {
                    ScriptDirectory directory = activeObjects.get(ScriptDirectory.class, entityId);
                    name = directory.getName();
                    deleted = directory.isDeleted();
                    if (directory.getParent() != null) {
                        parentName = ScriptUtil.getExpandedName(directory);
                    }
                    break;
                }
                case LISTENER: {
                    Listener listener = activeObjects.get(Listener.class, entityId);
                    name = listener.getName();
                    deleted = listener.isDeleted();
                    break;
                }
                case REST: {
                    RestScript script = activeObjects.get(RestScript.class, entityId);
                    name = script.getName();
                    deleted = script.isDeleted();
                    break;
                }
                case CUSTOM_FIELD: {
                    name = customFieldHelper.getFieldName(activeObjects.get(FieldConfig.class, entityId).getFieldConfigId());
                    deleted = false;
                    break;
                }
                case SCHEDULED_TASK: {
                    ScheduledTask task = activeObjects.get(ScheduledTask.class, entityId);
                    name = task.getName();
                    deleted = task.isDeleted();
                    break;
                }
            }
            result.setScriptName(name);
            result.setParentName(parentName);
            result.setDeleted(deleted);
            result.setScriptId(entityId);
        }

        return result;
    }
}
