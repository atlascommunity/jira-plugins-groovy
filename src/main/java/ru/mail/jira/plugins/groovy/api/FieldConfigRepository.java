package ru.mail.jira.plugins.groovy.api;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigDto;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;

import java.util.List;

public interface FieldConfigRepository {
    List<FieldConfigDto> getAllConfigs();

    FieldConfigDto getConfig(long id, boolean includeChangelogs);

    FieldConfigDto updateConfig(ApplicationUser user, long id, FieldConfigForm form);

    FieldScript getScript(long fieldConfigId);

    void invalidateAll();
}
