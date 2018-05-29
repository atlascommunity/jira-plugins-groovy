package ru.mail.jira.plugins.groovy.api.repository;

import ru.mail.jira.plugins.groovy.api.dto.ScriptExecutionDto;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface ExecutionRepository {
    void trackFromRegistry(int id, long time, boolean successful, String error, Map<String, String> additionalParams);

    void trackInline(String id, long time, boolean successful, String error, Map<String, String> additionalParams);

    Map<Integer, Long> getRegistryErrorCount();

    List<ScriptExecutionDto> getRegistryExecutions(int scriptId);

    List<ScriptExecutionDto> getInlineExecutions(String scriptId);

    List<ScriptExecutionDto> getLastRegistryExecutions(int scriptId);

    List<ScriptExecutionDto> getLastInlineExecutions(String scriptId);

    void deleteOldExecutions();

    void deleteExecutions(int scriptId, Timestamp until);

    int getErrorCount(int id);

    int getErrorCount(String id);
}
