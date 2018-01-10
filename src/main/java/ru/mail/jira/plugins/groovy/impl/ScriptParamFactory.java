package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

@Component
public class ScriptParamFactory {
    private final UserManager userManager;
    private final CustomFieldManager customFieldManager;
    private final GroupManager groupManager;

    @Autowired
    public ScriptParamFactory(
        @ComponentImport UserManager userManager,
        @ComponentImport CustomFieldManager customFieldManager,
        @ComponentImport GroupManager groupManager
    ) {
        this.userManager = userManager;
        this.customFieldManager = customFieldManager;
        this.groupManager = groupManager;
    }

    public Object getParamObject(ScriptParamDto paramDto, String value) {
        if (value == null) {
            return null;
        }

        switch (paramDto.getParamType()) {
            case STRING:
            case TEXT:
                return value;
            case LONG:
                return Longs.tryParse(value);
            case DOUBLE:
                return Doubles.tryParse(value);
            case CUSTOM_FIELD:
                return customFieldManager.getCustomFieldObject(value);
            case USER:
                return userManager.getUserByKey(value);
            case GROUP:
                return groupManager.getGroup(value);
        }

        return null;
    }
}
