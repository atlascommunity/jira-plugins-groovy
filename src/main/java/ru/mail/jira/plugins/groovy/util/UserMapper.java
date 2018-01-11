package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.JiraUser;

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

    public JiraUser buildUser(ApplicationUser currentUser, String key) {
        ApplicationUser user = userManager.getUserByKey(key);

        if (user == null) {
            return new JiraUser(key, null, key);
        }

        return new JiraUser(
            user.getName(),
            avatarService.getAvatarURL(currentUser, user).toString(),
            user.getDisplayName()
        );
    }

    public JiraUser buildUser(String key) {
        return buildUser(authenticationContext.getLoggedInUser(), key);
    }
}
