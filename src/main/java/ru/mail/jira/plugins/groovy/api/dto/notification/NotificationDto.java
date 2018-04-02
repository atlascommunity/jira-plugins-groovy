package ru.mail.jira.plugins.groovy.api.dto.notification;

import com.atlassian.jira.user.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;

@AllArgsConstructor @Getter
public class NotificationDto {
    private final ApplicationUser user;
    private final EntityAction action;
    private final EntityType entityType;
    private final String entityName;
    private final Integer entityId;
    private final String diff;
    private final String templateDiff;
    private final String comment;
}
