package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.pocketknife.api.querydsl.util.OnRollback;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import net.java.ao.DBParam;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.mail.jira.plugins.groovy.util.QueryDslTables.AUDIT_LOG_ENTRY;
import static ru.mail.jira.plugins.groovy.util.QueryDslTables.AUDIT_LOG_ISSUE_RELATION;

@Component
@ExportAsDevService(AuditLogRepository.class)
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final IssueManager issueManager;
    private final ActiveObjects activeObjects;
    private final DateTimeFormatter dateTimeFormatter;
    private final DatabaseAccessor databaseAccessor;
    private final CustomFieldHelper customFieldHelper;
    private final UserMapper userMapper;

    @Autowired
    public AuditLogRepositoryImpl(
        @ComponentImport IssueManager issueManager,
        @ComponentImport ActiveObjects activeObjects,
        @ComponentImport DateTimeFormatter dateTimeFormatter,
        DatabaseAccessor databaseAccessor,
        CustomFieldHelper customFieldHelper,
        UserMapper userMapper
    ) {
        this.issueManager = issueManager;
        this.activeObjects = activeObjects;
        this.dateTimeFormatter = dateTimeFormatter;
        this.databaseAccessor = databaseAccessor;
        this.customFieldHelper = customFieldHelper;
        this.userMapper = userMapper;
    }

    @Override
    public void create(ApplicationUser user, AuditLogEntryForm form) {
        AuditLogEntry auditLogEntry = activeObjects.create(
            AuditLogEntry.class,
            new DBParam("DATE", new Timestamp(System.currentTimeMillis())),
            new DBParam("USER_KEY", user.getKey()),
            new DBParam("CATEGORY", form.getCategory()),
            new DBParam("ACTION", form.getAction()),
            new DBParam("DESCRIPTION", form.getDescription()),
            new DBParam("ENTITY_ID", form.getEntityId())
        );

        createRelations(auditLogEntry);
    }

    @Override
    public List<AuditLogEntryDto> findAllForEntity(int id, EntityType entityType) {
        return databaseAccessor.run(
            connection -> connection
                .select(AUDIT_LOG_ENTRY.all())
                .from(AUDIT_LOG_ENTRY)
                .where(
                    AUDIT_LOG_ENTRY.CATEGORY.eq(entityType.name()),
                    AUDIT_LOG_ENTRY.ENTITY_ID.eq(id)
                )
                .orderBy(AUDIT_LOG_ENTRY.ID.asc())
                .fetch()
                .stream()
                .map(this::buildDto)
                .collect(Collectors.toList()),
            OnRollback.NOOP
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
                .from(AUDIT_LOG_ENTRY)
                .where(conditionsArray)
                .orderBy(AUDIT_LOG_ENTRY.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetch()
                .stream()
                .map(this::buildDto)
                .collect(Collectors.toList());

            long totalCount = connection
                .select()
                .from(AUDIT_LOG_ENTRY)
                .where(conditionsArray)
                .fetchCount();

            return new Page<>(offset, limit, totalCount, items);
        }, OnRollback.NOOP);
    }

    @Override
    public void createRelations(AuditLogEntry entry) {
        EntityAction action = entry.getAction();
        if (action == EntityAction.MOVED || entry.getCategory() == EntityType.REGISTRY_DIRECTORY) {
            return;
        }

        Set<String> issueKeys = new HashSet<>(JiraKeyUtils.getIssueKeysFromString(entry.getDescription()));

        Integer entityId = entry.getEntityId();
        //search for issue key in name only for CREATED action
        if (action == EntityAction.CREATED && entityId != null) {
            String name = null;
            switch (entry.getCategory()) {
                case ADMIN_SCRIPT: {
                    AdminScript script = activeObjects.get(AdminScript.class, entityId);
                    name = script.getName();
                    break;
                }
                case REGISTRY_SCRIPT: {
                    Script script = activeObjects.get(Script.class, entityId);
                    name = script.getName();
                    break;
                }
                case REGISTRY_DIRECTORY: {
                    ScriptDirectory directory = activeObjects.get(ScriptDirectory.class, entityId);
                    name = directory.getName();
                    break;
                }
                case LISTENER: {
                    Listener listener = activeObjects.get(Listener.class, entityId);
                    name = listener.getName();
                    break;
                }
                case REST: {
                    RestScript script = activeObjects.get(RestScript.class, entityId);
                    name = script.getName();
                    break;
                }
                case CUSTOM_FIELD: {
                    Long fieldConfigId = activeObjects.get(FieldScript.class, entityId).getFieldConfigId();
                    name = customFieldHelper.getFieldName(fieldConfigId);
                    break;
                }
                case SCHEDULED_TASK: {
                    ScheduledTask task = activeObjects.get(ScheduledTask.class, entityId);
                    name = task.getName();
                    break;
                }
                case JQL_FUNCTION:
                    JqlFunctionScript function = activeObjects.get(JqlFunctionScript.class, entityId);
                    name = function.getName();
                    break;
                case GLOBAL_OBJECT:
                    GlobalObject globalObject = activeObjects.get(GlobalObject.class, entityId);
                    name = globalObject.getName();
                    break;
            }
            if (name != null) {
                issueKeys.addAll(JiraKeyUtils.getIssueKeysFromString(name));
            }
        }

        databaseAccessor.run(
            connection -> connection
                .delete(AUDIT_LOG_ISSUE_RELATION)
                .where(AUDIT_LOG_ISSUE_RELATION.AUDIT_LOG_ID.eq(entry.getID()))
                .execute(),
            OnRollback.NOOP
        );
        for (String issueKey : issueKeys) {
            MutableIssue issue = issueManager.getIssueObject(issueKey);

            if (issue != null) {
                activeObjects.create(
                    AuditLogIssueRelation.class,
                    new DBParam("AUDIT_LOG_ID", entry.getID()),
                    new DBParam("ISSUE_ID", issue.getId())
                );
            }
        }
    }

    @Override
    public List<AuditLogEntryDto> getRelated(long issueId) {
        return databaseAccessor.run(connection ->
            connection
                .select(AUDIT_LOG_ENTRY.all())
                .from(AUDIT_LOG_ENTRY)
                .join(AUDIT_LOG_ISSUE_RELATION).on(AUDIT_LOG_ENTRY.ID.eq(AUDIT_LOG_ISSUE_RELATION.AUDIT_LOG_ID))
                .where(AUDIT_LOG_ISSUE_RELATION.ISSUE_ID.eq(issueId))
                .orderBy(AUDIT_LOG_ENTRY.ID.desc())
                .fetch()
                .stream()
                .map(this::buildDto)
                .collect(Collectors.toList()),
            OnRollback.NOOP
        );
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
                    Long fieldConfigId = activeObjects.get(FieldScript.class, entityId).getFieldConfigId();
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
                case GLOBAL_OBJECT:
                    GlobalObject globalObject = activeObjects.get(GlobalObject.class, entityId);
                    name = globalObject.getName();
                    deleted = globalObject.isDeleted();
                    break;
            }
            result.setScriptName(name);
            result.setParentName(parentName);
            result.setDeleted(deleted);
            result.setUrl(ScriptUtil.getPermalink(category, result.getScriptId()));
        }
    }

    private AuditLogEntryDto buildDto(Tuple row) {
        AuditLogEntryDto result = new AuditLogEntryDto();

        EntityType category = EntityType.valueOf(row.get(AUDIT_LOG_ENTRY.CATEGORY));
        Integer entityId = row.get(AUDIT_LOG_ENTRY.ENTITY_ID);

        result.setDate(dateTimeFormatter.forLoggedInUser().format(row.get(AUDIT_LOG_ENTRY.DATE)));
        result.setId(row.get(AUDIT_LOG_ENTRY.ID));
        result.setUser(userMapper.buildUser(row.get(AUDIT_LOG_ENTRY.USER_KEY)));
        result.setAction(EntityAction.valueOf(row.get(AUDIT_LOG_ENTRY.ACTION)));
        result.setCategory(category);
        result.setDescription(row.get(AUDIT_LOG_ENTRY.DESCRIPTION));

        fillEntityData(result, category, entityId);

        return result;
    }
}
