package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.JiraUser;
import ru.mail.jira.plugins.groovy.impl.dto.PickerOption;

@Component
public class UserMapper {
    private final JiraAuthenticationContext authenticationContext;
    private final UserManager userManager;
    private final AvatarService avatarService;

    @Autowired
    public UserMapper(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        @ComponentImport UserManager userManager,
        @ComponentImport AvatarService avatarService
    ) {
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        this.avatarService = avatarService;
    }

    public JiraUser buildUserNullable(ApplicationUser currentUser, String key) {
        ApplicationUser user = userManager.getUserByKey(key);

        if (user == null) {
            return null;
        }

        return new JiraUser(
            user.getName(),
            avatarService.getAvatarURL(currentUser, user).toString(),
            user.getDisplayName()
        );
    }

    public JiraUser buildUser(ApplicationUser currentUser, String key) {
        JiraUser result = buildUserNullable(currentUser, key);

        if (result == null) {
            return new JiraUser(key, null, key);
        }

        return result;
    }

    public JiraUser buildUser(String key) {
        return buildUser(authenticationContext.getLoggedInUser(), key);
    }

    public JiraUser buildUserNullable(String key) {
        return buildUserNullable(authenticationContext.getLoggedInUser(), key);
    }

    public PickerOption buildUserOption(String key) {
        JiraUser jiraUser = buildUserNullable(key);

        if (jiraUser != null) {
            return new PickerOption(jiraUser.getDisplayName(), key, jiraUser.getAvatarUrl());
        }

        return null;
    }
}
