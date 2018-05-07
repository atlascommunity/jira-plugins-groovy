package ru.mail.jira.plugins.groovy.api.service.admin;

import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptDto;

import java.util.List;

public interface BuiltInScriptManager {
    List<AdminScriptDto> getAllScripts();

    BuiltInScript getScript(String key);
}
