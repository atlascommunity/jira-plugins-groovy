package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.notification.NotificationDto;

import java.util.List;

public interface NotificationService {
    void sendNotifications(NotificationDto notificationDto, List<ApplicationUser> recipients);
}
