package ru.mail.jira.plugins.groovy.api.service.admin;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import java.util.List;
import java.util.Map;

public interface BuiltInScript {
    String run(ApplicationUser currentUser, Map<String, Object> params) throws Exception;

    String getKey();

    String getI18nKey();

    boolean isHtml();

    List<ScriptParamDto> getParams();
}
