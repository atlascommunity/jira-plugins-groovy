package ru.mail.jira.plugins.groovy.api.repository;

import ru.mail.jira.plugins.groovy.api.dto.execution.ScriptExecutionDto;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface ExecutionRepository {
    long WARNING_THRESHOLD = TimeUnit.SECONDS.toMillis(2);

    void trackFromRegistry(int id, long time, boolean successful, Exception e, Map<String, String> additionalParams);

    void trackInline(String id, long time, boolean successful, Exception e, Map<String, String> additionalParams);

    Map<Integer, Long> getRegistryErrorCount();

    Map<Integer, Long> getRegistryWarningCount();

    List<ScriptExecutionDto> getRegistryExecutions(int scriptId);

    List<ScriptExecutionDto> getInlineExecutions(String scriptId);

    List<ScriptExecutionDto> getLastRegistryExecutions(int scriptId);

    List<ScriptExecutionDto> getLastInlineExecutions(String scriptId);

    void deleteOldExecutions();

    void deleteExecutions(int scriptId, Timestamp until);

    void deleteAll();

    int getErrorCount(int id);

    int getErrorCount(String id);

    Map<String, Long> getErrorCount(Set<String> ids);

    int getWarningCount(int id);

    int getWarningCount(String id);

    Map<String, Long> getWarningCount(Set<String> ids);
}
