package ru.mail.jira.plugins.groovy.impl;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.GlobalFunctionManager;

import java.util.Map;

@Component
public class GlobalFunctionManagerImpl implements GlobalFunctionManager {
    public Map<String, String> getGlobalFunctions() {
        return ImmutableMap.of(
            "getCurrentUser", "com.atlassian.jira.component.ComponentAccessor.jiraAuthenticationContext.loggedInUser",
            "getField", "com.atlassian.jira.component.ComponentAccessor.customFieldManager.getCustomFieldObject(id)",
            "escaped", "import org.apache.commons.lang3.StringEscapeUtils\n" + "\n" + "StringEscapeUtils.escapeHtml4(str)"
        );
    }
}
