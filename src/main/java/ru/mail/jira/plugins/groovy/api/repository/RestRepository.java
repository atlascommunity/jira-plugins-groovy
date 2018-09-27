package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.rest.HttpMethod;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptForm;
import ru.mail.jira.plugins.groovy.api.dto.rest.Script;

import java.util.List;

public interface RestRepository {
    List<RestScriptDto> getAllScripts();

    @Transactional
    RestScriptDto createScript(ApplicationUser user, RestScriptForm form);

    RestScriptDto getScript(int id, boolean includeChangelogs, boolean includeErrorCount);

    @Transactional
    RestScriptDto updateScript(ApplicationUser user, int id, RestScriptForm form);

    @Transactional
    void deleteScript(ApplicationUser user, int id);

    @Transactional
    void restoreScript(ApplicationUser user, int id);

    Script getScript(HttpMethod method, String key);
}
