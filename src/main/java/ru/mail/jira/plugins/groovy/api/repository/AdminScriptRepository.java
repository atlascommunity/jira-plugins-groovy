package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptForm;

import java.util.List;

public interface AdminScriptRepository {
    List<AdminScriptDto> getAllScripts();

    List<ChangelogDto> getChangelogs(int id);

    @Transactional
    AdminScriptDto createScript(ApplicationUser user, AdminScriptForm form);

    AdminScriptDto getScript(int id, boolean includeChangelogs, boolean includeErrorCount);

    @Transactional
    AdminScriptDto updateScript(ApplicationUser user, int id, AdminScriptForm form);

    @Transactional
    void deleteScript(ApplicationUser user, int id);

    @Transactional
    void restoreScript(ApplicationUser user, int id);
}
