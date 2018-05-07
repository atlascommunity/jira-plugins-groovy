package ru.mail.jira.plugins.groovy.api.service.admin;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptOutcome;

import java.util.Map;

public interface AdminScriptService {
    AdminScriptOutcome runBuiltInScript(ApplicationUser user, String key, Map<String, String> params) throws Exception;

    AdminScriptOutcome runUserScript(ApplicationUser user, Integer id, Map<String, String> params) throws Exception;
}
