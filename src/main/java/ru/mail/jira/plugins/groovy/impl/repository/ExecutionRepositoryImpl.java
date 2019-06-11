package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.adapter.jackson.ObjectMapper;
import com.atlassian.jira.cluster.ClusterInfo;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.pocketknife.api.querydsl.util.OnRollback;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.Union;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.dto.execution.ScriptExecutionDto;
import ru.mail.jira.plugins.groovy.api.entity.ScriptExecution;
import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.mail.jira.plugins.groovy.util.QueryDslTables.REGISTRY_SCRIPT;
import static ru.mail.jira.plugins.groovy.util.QueryDslTables.SCRIPT_EXECUTION;

//todo: consider deleting executions when their count > 50
@Component
public class ExecutionRepositoryImpl implements ExecutionRepository, PluginLifecycleAware {
    private final Logger logger = LoggerFactory.getLogger(ExecutionRepositoryImpl.class);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
        ThreadFactories.namedThreadFactory("MAILRU_GROOVY_BG_THREAD")
    );
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ActiveObjects ao;
    private final ClusterInfo clusterInfo;
    private final DateTimeFormatter dateTimeFormatter;
    private final DatabaseAccessor databaseAccessor;

    @Autowired
    public ExecutionRepositoryImpl(
        @ComponentImport ActiveObjects ao,
        @ComponentImport ClusterInfo clusterInfo,
        @ComponentImport DateTimeFormatter dateTimeFormatter,
        DatabaseAccessor databaseAccessor
    ) {
        this.ao = ao;
        this.clusterInfo = clusterInfo;
        this.dateTimeFormatter = dateTimeFormatter;
        this.databaseAccessor = databaseAccessor;
    }

    @Override
    public void trackFromRegistry(int id, long time, boolean successful, String error, Map<String, String> additionalParams) {
        executorService.execute(() -> {
            try {
                this.saveExecution(id, time, successful, error, objectMapper.writeValueAsString(getParams(additionalParams)));
            } catch (Exception e) {
                logger.error("unable to save execution", e);
            }
        });
    }

    @Override
    public void trackInline(String id, long time, boolean successful, String error, Map<String, String> additionalParams) {
        executorService.execute(() -> {
            try {
                this.saveExecution(id, time, successful, error, objectMapper.writeValueAsString(getParams(additionalParams)));
            } catch (Exception e) {
                logger.error("unable to save execution", e);
            }
        });
    }

    private Union<Integer> buildUnionScriptQuery(NumberPath<Integer> scriptId, BooleanExpression whereClause) {
        return SQLExpressions.unionAll(
            SQLExpressions
                .select(REGISTRY_SCRIPT.ID.as(scriptId))
                .from(REGISTRY_SCRIPT)
                .join(SCRIPT_EXECUTION)
                .on(
                    SCRIPT_EXECUTION.SCRIPT_ID.eq(REGISTRY_SCRIPT.ID),
                    REGISTRY_SCRIPT.UUID.isNull()
                )
                .where(whereClause),
            SQLExpressions
                .select(REGISTRY_SCRIPT.ID.as(scriptId))
                .from(REGISTRY_SCRIPT)
                .join(SCRIPT_EXECUTION)
                .on(SCRIPT_EXECUTION.INLINE_ID.eq(REGISTRY_SCRIPT.UUID))
                .where(whereClause)
        );
    }

    @Override
    public Map<Integer, Long> getRegistryErrorCount() {
        NumberPath<Integer> scriptId = Expressions.numberPath(Integer.class, "scriptId");

        return databaseAccessor.run(connection ->
                connection
                    .select(scriptId, Wildcard.count)
                    .from(buildUnionScriptQuery(scriptId, SCRIPT_EXECUTION.SUCCESSFUL.isFalse()).as("temp"))
                    .groupBy(scriptId)
                    .fetch()
                    .stream()
                    .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Integer.class),
                        tuple -> tuple.get(1, Long.class),
                        (a, b) -> a + b
                    )),
            OnRollback.NOOP
        );
    }

    @Override
    public Map<Integer, Long> getRegistryWarningCount() {
        NumberPath<Integer> scriptId = Expressions.numberPath(Integer.class, "scriptId");

        return databaseAccessor.run(connection ->
                connection
                    .select(scriptId, Wildcard.count)
                    .from(
                        buildUnionScriptQuery(
                            scriptId,
                            Expressions.allOf(
                                SCRIPT_EXECUTION.SUCCESSFUL.isTrue(),
                                SCRIPT_EXECUTION.TIME.goe(WARNING_THRESHOLD)
                            )
                        ).as("temp")
                    )
                    .groupBy(scriptId)
                    .fetch()
                    .stream()
                    .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Integer.class),
                        tuple -> tuple.get(1, Long.class),
                        (a, b) -> a + b
                    )),
            OnRollback.NOOP
        );
    }

    @Override
    public List<ScriptExecutionDto> getRegistryExecutions(int scriptId) {
        return Arrays
            .stream(ao.find(ScriptExecution.class, Query.select().where("SCRIPT_ID = ?", scriptId)))
            .map(this::buildDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<ScriptExecutionDto> getInlineExecutions(String scriptId) {
        return Arrays
            .stream(ao.find(ScriptExecution.class, Query.select().where("INLINE_ID = ?", scriptId)))
            .map(this::buildDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<ScriptExecutionDto> getLastRegistryExecutions(int scriptId) {
        return Arrays
            .stream(ao.find(ScriptExecution.class, Query.select().where("SCRIPT_ID = ?", scriptId).order("ID DESC").limit(25)))
            .map(this::buildDto)
            .sorted(Comparator.comparingInt(ScriptExecutionDto::getId))
            .collect(Collectors.toList());
    }

    @Override
    public List<ScriptExecutionDto> getLastInlineExecutions(String scriptId) {
        return Arrays
            .stream(ao.find(ScriptExecution.class, Query.select().where("INLINE_ID = ?", scriptId).order("ID DESC").limit(25)))
            .map(this::buildDto)
            .sorted(Comparator.comparingInt(ScriptExecutionDto::getId))
            .collect(Collectors.toList());
    }

    private Map<String, String> getParams(Map<String, String> source) {
        if (clusterInfo.isClustered()) {
            Map<String, String> params = new HashMap<>(source);
            params.put("cluster_node", clusterInfo.getNodeId());
            return params;
        }
        return source;
    }

    private ScriptExecutionDto buildDto(ScriptExecution execution) {
        ScriptExecutionDto result = new ScriptExecutionDto();

        String id = execution.getInlineId();
        if (id != null) {
            result.setScriptId(id);
        } else {
            result.setScriptId(String.valueOf(execution.getScript().getID()));
        }
        result.setDate(dateTimeFormatter.forLoggedInUser().format(execution.getDate()));

        result.setTime(execution.getTime());
        result.setSuccess(execution.isSuccessful());
        result.setSlow(execution.getTime() >= WARNING_THRESHOLD);
        result.setError(execution.getError());
        result.setExtraParams(execution.getExtraParams());
        result.setId(execution.getID());

        return result;
    }

    private void saveExecution(int id, long time, boolean successful, String error, String additionalParams) {
        ao.create(
            ScriptExecution.class,
            new DBParam("SCRIPT_ID", id),
            new DBParam("TIME", time),
            new DBParam("DATE", new Timestamp(System.currentTimeMillis())),
            new DBParam("SUCCESSFUL", successful),
            new DBParam("ERROR", error),
            new DBParam("EXTRA_PARAMS", additionalParams)
        );
    }

    private void saveExecution(String id, long time, boolean successful, String error, String additionalParams) {
        ao.create(
            ScriptExecution.class,
            new DBParam("INLINE_ID", id),
            new DBParam("TIME", time),
            new DBParam("DATE", new Timestamp(System.currentTimeMillis())),
            new DBParam("SUCCESSFUL", successful),
            new DBParam("ERROR", error),
            new DBParam("EXTRA_PARAMS", additionalParams)
        );
    }

    @Override
    public int getErrorCount(int id) {
        return ao.count(ScriptExecution.class, Query.select().where("SCRIPT_ID = ? AND SUCCESSFUL = ?", id, Boolean.FALSE));
    }

    @Override
    public int getErrorCount(String id) {
        return ao.count(ScriptExecution.class, Query.select().where("INLINE_ID = ? AND SUCCESSFUL = ?", id, Boolean.FALSE));
    }

    @Override
    public Map<String, Long> getErrorCount(Set<String> ids) {
        if (ids.isEmpty()) {
            return ImmutableMap.of();
        }

        return databaseAccessor.run(connection ->
            connection
                .select(SCRIPT_EXECUTION.INLINE_ID.as("id"), SQLExpressions.count().as("count"))
                .from(SCRIPT_EXECUTION)
                .where(
                    SCRIPT_EXECUTION.INLINE_ID.in(ids),
                    SCRIPT_EXECUTION.SUCCESSFUL.isFalse()
                )
                .groupBy(SCRIPT_EXECUTION.INLINE_ID)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                    row -> row.get(0, String.class),
                    row -> row.get(1, Long.class)
                ))
            ,
            OnRollback.NOOP
        );
    }

    @Override
    public int getWarningCount(int id) {
        return ao.count(ScriptExecution.class, Query.select().where("SCRIPT_ID = ? AND TIME >= ?", id, WARNING_THRESHOLD));
    }

    @Override
    public int getWarningCount(String id) {
        return ao.count(ScriptExecution.class, Query.select().where("INLINE_ID = ? AND TIME >= ?", id, WARNING_THRESHOLD));
    }

    @Override
    public Map<String, Long> getWarningCount(Set<String> ids) {
        if (ids.isEmpty()) {
            return ImmutableMap.of();
        }

        return databaseAccessor.run(connection ->
                connection
                    .select(SCRIPT_EXECUTION.INLINE_ID.as("id"), SQLExpressions.count().as("count"))
                    .from(SCRIPT_EXECUTION)
                    .where(
                        SCRIPT_EXECUTION.INLINE_ID.in(ids),
                        SCRIPT_EXECUTION.SUCCESSFUL.isTrue(),
                        SCRIPT_EXECUTION.TIME.goe(WARNING_THRESHOLD)
                    )
                    .groupBy(SCRIPT_EXECUTION.INLINE_ID)
                    .fetch()
                    .stream()
                    .collect(Collectors.toMap(
                        row -> row.get(0, String.class),
                        row -> row.get(1, Long.class)
                    ))
            ,
            OnRollback.NOOP
        );
    }

    @Override
    public void deleteAll() {
        databaseAccessor.run(connection ->
            connection
                .delete(SCRIPT_EXECUTION)
                .execute(),
            OnRollback.NOOP
        );
    }

    @Override
    public void deleteOldExecutions() {
        int deleted = ao.deleteWithSQL(ScriptExecution.class, "DATE < ?", new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14)));
        logger.info("Deleted {} old executions", deleted);
    }

    @Override
    public void deleteExecutions(int scriptId, Timestamp until) {
        databaseAccessor.run(
            connection -> connection
                .delete(SCRIPT_EXECUTION)
                .where(
                    SCRIPT_EXECUTION.SCRIPT_ID.eq(scriptId),
                    SCRIPT_EXECUTION.DATE.before(until)
                )
                .execute(),
            OnRollback.NOOP
        );
    }

    @Override
    public void onStart() {}

    @Override
    public void onStop() {
        executorService.shutdown();
    }

    @Override
    public int getInitOrder() {
        return 1001;
    }
}
