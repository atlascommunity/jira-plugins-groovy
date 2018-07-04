package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.builder.EmailBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.notification.NotificationDto;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class NotificationServiceImpl implements NotificationService {
    private static final String NOTIFY_OWN_CHANGES_PROPERTY_KEY = "user.notify.own.changes";

    private final MailQueue mailQueue;
    private final I18nResolver i18nResolver;
    private final LocaleManager localeManager;
    private final UserPreferencesManager userPreferencesManager;
    private final PermissionHelper permissionHelper;

    @Autowired
    public NotificationServiceImpl(
        @ComponentImport MailQueue mailQueue,
        @ComponentImport I18nResolver i18nResolver,
        @ComponentImport LocaleManager localeManager,
        @ComponentImport UserPreferencesManager userPreferencesManager,
        PermissionHelper permissionHelper
    ) {
        this.mailQueue = mailQueue;
        this.i18nResolver = i18nResolver;
        this.localeManager = localeManager;
        this.userPreferencesManager = userPreferencesManager;
        this.permissionHelper = permissionHelper;
    }

    @Override
    public void sendNotifications(NotificationDto notificationDto, List<ApplicationUser> recipients) {
        String permalink = getPermalink(notificationDto.getEntityType(), notificationDto.getEntityId());

        for (ApplicationUser recipient : recipients) {
            if (!permissionHelper.isAdmin(recipient)) {
                continue;
            }

            Preferences usersPrefs = userPreferencesManager.getPreferences(recipient);
            if (notificationDto.getUser().equals(recipient) && !usersPrefs.getBoolean(NOTIFY_OWN_CHANGES_PROPERTY_KEY)) {
                continue;
            }

            Locale locale = localeManager.getLocaleFor(recipient);

            String actionName = i18nResolver.getText(locale, notificationDto.getAction().getI18nName());
            String typeName = i18nResolver.getText(locale, notificationDto.getEntityType().getI18nName());

            String subject = actionName + " " + typeName + " - " + notificationDto.getEntityName();

            Map<String, Object> params = new HashMap<>();
            params.put("notification", notificationDto);
            params.put("pageTitle", subject);
            params.put("user", recipient);
            params.put("locale", locale);
            params.put("i18nResolver", i18nResolver);
            params.put("permalink", permalink);

            MailQueueItem email = new EmailBuilder(new Email(recipient.getEmailAddress()), new NotificationRecipient(recipient))
                .withSubject(subject)
                .withBodyFromFile("ru/mail/jira/plugins/groovy/templates/notification.vm")
                .addParameters(params)
                .renderLater();

            mailQueue.addItem(email);
        }
    }

    public String getPermalink(EntityType entityType, Integer id) {
        if (entityType.isSupportsPermalink() && id != null) {
            String pluginBaseUrl = "/plugins/servlet/my-groovy/";

            switch (entityType) {
                case REGISTRY_SCRIPT:
                    return pluginBaseUrl + "registry/script/view/" + id;
                case CUSTOM_FIELD:
                    return pluginBaseUrl + "fields/" + id + "/view";
                case ADMIN_SCRIPT:
                    return pluginBaseUrl + "admin-scripts/" + id + "/view";
                case LISTENER:
                    return pluginBaseUrl + "listeners/" + id + "/view";
                case REST:
                    return pluginBaseUrl + "rest/" + id + "/view";
            }

            return null;
        }

        return null;
    }
}
