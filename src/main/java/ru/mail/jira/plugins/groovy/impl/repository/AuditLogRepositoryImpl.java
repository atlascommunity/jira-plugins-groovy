package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.pocketknife.api.querydsl.util.OnRollback;
import com.querydsl.core.types.Predicate;
import net.java.ao.DBParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.entity.*;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryDto;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.Page;
import ru.mail.jira.plugins.groovy.impl.repository.querydsl.QAuditLogEntry;
import ru.mail.jira.plugins.groovy.util.CustomFieldHelper;
import ru.mail.jira.plugins.groovy.util.ScriptUtil;
import ru.mail.jira.plugins.groovy.util.UserMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private static final QAuditLogEntry AUDIT_LOG_ENTRY = new QAuditLogEntry();

    private final ActiveObjects activeObjects;
    private final DateTimeFormatter dateTimeFormatter;
    private final DatabaseAccessor databaseAccessor;
    private final CustomFieldHelper customFieldHelper;
    private final UserMapper userMapper;

    @Autowired
    public AuditLogRepositoryImpl(
        @ComponentImport ActiveObjects activeObjects,
        @ComponentImport DateTimeFormatter dateTimeFormatter,
        DatabaseAccessor databaseAccessor,
        CustomFieldHelper customFieldHelper,
        UserMapper userMapper
    ) {
        this.activeObjects = activeObjects;
        this.dateTimeFormatter = dateTimeFormatter;
        this.databaseAccessor = databaseAccessor;
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
    public Page<AuditLogEntryDto> getPagedEntries(int offset, int limit, Set<String> users, Set<EntityType> categories, Set<EntityAction> actions) {
        return databaseAccessor.run(connection -> {
            List<Predicate> conditions = new ArrayList<>();

            if (users.size() > 0) {
                conditions.add(AUDIT_LOG_ENTRY.USER_KEY.in(users));
            }

            if (categories.size() > 0) {
                conditions.add(AUDIT_LOG_ENTRY.CATEGORY.in(categories.stream().map(EntityType::name).collect(Collectors.toList())));
            }

            if (actions.size() > 0) {
                conditions.add(AUDIT_LOG_ENTRY.ACTION.in(actions.stream().map(EntityAction::name).collect(Collectors.toList())));
            }

            Predicate[] conditionsArray = conditions.toArray(new Predicate[0]);

            List<AuditLogEntryDto> items = connection
                .select(AUDIT_LOG_ENTRY.all())
                .from(
                    AUDIT_LOG_ENTRY
                )
                .where(conditionsArray)
                .orderBy(AUDIT_LOG_ENTRY.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetch()
                .stream()
                .map(row -> {
                    AuditLogEntryDto result = new AuditLogEntryDto();

                    EntityType category = EntityType.valueOf(row.get(AUDIT_LOG_ENTRY.CATEGORY));

                    result.setDate(dateTimeFormatter.forLoggedInUser().format(row.get(AUDIT_LOG_ENTRY.DATE)));
                    result.setId(row.get(AUDIT_LOG_ENTRY.ID));
                    result.setUser(userMapper.buildUser(row.get(AUDIT_LOG_ENTRY.USER_KEY)));
                    result.setAction(EntityAction.valueOf(row.get(AUDIT_LOG_ENTRY.ACTION)));
                    result.setCategory(category);
                    result.setDescription(row.get(AUDIT_LOG_ENTRY.DESCRIPTION));

                    fillEntityData(result, category, row.get(AUDIT_LOG_ENTRY.ENTITY_ID));

                    return result;
                })
                .collect(Collectors.toList());

            long totalCount = connection
                .select()
                .from(AUDIT_LOG_ENTRY)
                .where(conditionsArray)
                .fetchCount();

            return new Page<>(offset, limit, totalCount, items);
        }, OnRollback.NOOP);
    }

    private void fillEntityData(AuditLogEntryDto result, EntityType category, Integer entityId) {
        if (entityId != null) {
            result.setScriptId(entityId);

            String name = null;
            String parentName = null;
            Boolean deleted = null;
            switch (category) {
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
                    Long fieldConfigId = activeObjects.get(FieldConfig.class, entityId).getFieldConfigId();
                    result.setScriptId((int) (long) fieldConfigId);
                    name = customFieldHelper.getFieldName(fieldConfigId);
                    deleted = false;
                    break;
                }
                case SCHEDULED_TASK: {
                    ScheduledTask task = activeObjects.get(ScheduledTask.class, entityId);
                    name = task.getName();
                    deleted = task.isDeleted();
                    break;
                }
                case JQL_FUNCTION:
                    JqlFunctionScript function = activeObjects.get(JqlFunctionScript.class, entityId);
                    name = function.getName();
                    deleted = function.isDeleted();
                    break;
            }
            result.setScriptName(name);
            result.setParentName(parentName);
            result.setDeleted(deleted);
        }
    }
}
