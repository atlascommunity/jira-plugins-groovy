package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;

import java.util.List;
import java.util.Map;

@Component
public class DeleteAllExecutions implements BuiltInScript<String> {
    private final ExecutionRepository executionRepository;

    @Autowired
    public DeleteAllExecutions(
        ExecutionRepository executionRepository
    ) {
        this.executionRepository = executionRepository;
    }

    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) throws Exception {
        executionRepository.deleteAll();

        return "ok";
    }

    @Override
    public String getKey() {
        return "deleteAllExecutions";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.deleteAllExecution";
    }

    @Override
    public boolean isHtml() {
        return false;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of();
    }
}
