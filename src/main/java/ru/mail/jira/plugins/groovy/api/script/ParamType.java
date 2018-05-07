package ru.mail.jira.plugins.groovy.api.script;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.user.ApplicationUser;
import lombok.Getter;

@Getter
public enum ParamType {
    BOOLEAN(Boolean.class),
    STRING(String.class),
    TEXT(String.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    CUSTOM_FIELD(CustomField.class), //todo: system fields, script, current workflow action
    USER(ApplicationUser.class),
    GROUP(Group.class),
    RESOLUTION(Resolution.class);

    private final Class type;

    ParamType(Class type) {
        this.type = type;
    }
}
