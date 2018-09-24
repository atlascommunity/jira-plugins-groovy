package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionForm;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;

import java.util.List;

public interface JqlFunctionRepository {
    List<JqlFunctionScriptDto> getAllScripts(boolean includeChangelogs, boolean includeErrorCount);

    JqlFunctionScriptDto getScript(int id);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    JqlFunctionScriptDto createScript(ApplicationUser user, JqlFunctionForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    JqlFunctionScriptDto updateScript(ApplicationUser user, int id, JqlFunctionForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    void deleteScript(ApplicationUser user, int id);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.JqlFunctionService}
     */
    void restoreScript(ApplicationUser user, int id);
}
