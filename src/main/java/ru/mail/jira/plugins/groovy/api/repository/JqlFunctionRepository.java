package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionForm;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;

import java.util.List;

public interface JqlFunctionRepository {
    List<JqlFunctionScriptDto> getAllScripts(boolean includeScriptBody, boolean includeChangelogs, boolean includeErrorCount);

    List<ChangelogDto> getChangelogs(int id);

    JqlFunctionScriptDto getScript(int id);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    @Transactional
    JqlFunctionScriptDto createScript(ApplicationUser user, JqlFunctionForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    @Transactional
    JqlFunctionScriptDto updateScript(ApplicationUser user, int id, JqlFunctionForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    @Transactional
    void deleteScript(ApplicationUser user, int id);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    @Transactional
    void restoreScript(ApplicationUser user, int id);
}
