package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionForm;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;

public interface JqlFunctionService {
    JqlFunctionScriptDto createScript(ApplicationUser user, JqlFunctionForm form);

    JqlFunctionScriptDto updateScript(ApplicationUser user, int id, JqlFunctionForm form);

    void deleteScript(ApplicationUser user, int id);

    void restoreScript(ApplicationUser user, int id);
}
