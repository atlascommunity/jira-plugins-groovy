package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.adapter.jackson.ObjectMapper;
import com.atlassian.jira.cluster.ClusterInfo;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.pocketknife.api.querydsl.util.OnRollback;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.util.concurrent.ThreadFactories;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.dto.ScriptExecutionDto;
import ru.mail.jira.plugins.groovy.api.entity.ScriptExecution;
import ru.mail.jira.plugins.groovy.impl.repository.querydsl.QScriptExecution;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//todo: consider deleting executions when their count > 50
@Component
@ExportAsService(LifecycleAware.class)
public class ExecutionRepositoryImpl implements ExecutionRepository, LifecycleAware {
    private static final QScriptExecution SCRIPT_EXECUTION = new QScriptExecution();

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

    @Override
    public Map<Integer, Long> getRegistryErrorCount() {
        return databaseAccessor.run(connection ->
            connection
                .select(SCRIPT_EXECUTION.SCRIPT_ID, SCRIPT_EXECUTION.ID.count())
                .from(SCRIPT_EXECUTION)
                .where(
                    SCRIPT_EXECUTION.SCRIPT_ID.isNotNull(),
                    SCRIPT_EXECUTION.SUCCESSFUL.isFalse()
                )
                .groupBy(SCRIPT_EXECUTION.SCRIPT_ID)
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
}
