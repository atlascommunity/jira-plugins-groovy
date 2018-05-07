package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptForm;

import java.util.List;

public interface AdminScriptRepository {
    List<AdminScriptDto> getAllScripts();

    AdminScriptDto createScript(ApplicationUser user, AdminScriptForm form);

    AdminScriptDto getScript(int id, boolean includeChangelogs, boolean includeErrorCount);

    AdminScriptDto updateScript(ApplicationUser user, int id, AdminScriptForm form);

    void deleteScript(ApplicationUser user, int id);

    void restoreScript(ApplicationUser user, int id);
}
