package ru.mail.jira.plugins.groovy.api.service.admin;

import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import java.util.List;
import java.util.Map;

public interface BuiltInScript<T> {
    T run(ApplicationUser currentUser, Map<String, Object> params) throws Exception;

    String getKey();

    String getI18nKey();

    default String getResultWidth() {
        return "medium";
    }

    boolean isHtml();

    List<ScriptParamDto> getParams();

    default Map<String, Object> getDefaultValues() {
        return ImmutableMap.of();
    }
}
