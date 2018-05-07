package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.JiraUser;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.util.UserMapper;

@Component
public class ScriptParamFactory {
    private final UserManager userManager;
    private final CustomFieldManager customFieldManager;
    private final GroupManager groupManager;
    private final ConstantsManager constantsManager;
    private final UserMapper userMapper;

    @Autowired
    public ScriptParamFactory(
        @ComponentImport UserManager userManager,
        @ComponentImport CustomFieldManager customFieldManager,
        @ComponentImport GroupManager groupManager,
        @ComponentImport ConstantsManager constantsManager,
        UserMapper userMapper
    ) {
        this.constantsManager = constantsManager;
        this.userMapper = userMapper;
        this.userManager = userManager;
        this.customFieldManager = customFieldManager;
        this.groupManager = groupManager;
    }

    public Object getParamObject(ScriptParamDto paramDto, String value) {
        if (value == null) {
            return null;
        }

        switch (paramDto.getParamType()) {
            case BOOLEAN:
                return Boolean.valueOf(value);
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
            case RESOLUTION:
                return constantsManager.getResolution(value);
        }

        return null;
    }

    public Object getParamFormValue(ScriptParamDto paramDto, String value) {
        switch (paramDto.getParamType()) {
            case BOOLEAN:
                return Boolean.valueOf(value);
            case STRING:
            case TEXT:
            case LONG:
            case DOUBLE:
                return value;
            case CUSTOM_FIELD:
                CustomField customFieldObject = customFieldManager.getCustomFieldObject(value);
                if (customFieldObject == null) {
                    return null;
                }

                return ImmutableMap.of(
                    "label", customFieldObject.getName(),
                    "value", value
                );
            case USER:
                JiraUser user = userMapper.buildUserNullable(value);

                if (user == null) {
                    return null;
                }

                return ImmutableMap.of(
                    "label", user.getDisplayName(),
                    "value", value,
                    "avatarUrl", user.getAvatarUrl()
                );
            case GROUP:
                return ImmutableMap.of(
                    "label", value,
                    "value", value
                );
            case RESOLUTION:
                Resolution resolution = constantsManager.getResolution(value);
                return ImmutableMap.of(
                    "label", resolution != null ? resolution.getName() : value,
                    "value", value
                );
        }
        return null;
    }
}
