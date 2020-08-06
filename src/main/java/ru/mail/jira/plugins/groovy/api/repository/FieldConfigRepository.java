package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigDto;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;

import java.util.List;

public interface FieldConfigRepository {
    List<FieldConfigDto> getAllConfigs();

    List<ChangelogDto> getChangelogs(long id);

    FieldConfigDto getConfig(long id, boolean includeChangelogs);

    FieldConfigDto updateConfig(ApplicationUser user, long id, FieldConfigForm form);

    FieldScriptDto getScript(long fieldConfigId);

    void invalidateAll();
}
