package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionHelper {
    private final JiraAuthenticationContext authenticationContext;
    private final GlobalPermissionManager globalPermissionManager;

    @Autowired
    public PermissionHelper(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        @ComponentImport GlobalPermissionManager globalPermissionManager
    ) {
        this.authenticationContext = authenticationContext;
        this.globalPermissionManager = globalPermissionManager;
    }

    public void checkIfAdmin() {
        checkIfAdmin(authenticationContext.getLoggedInUser());
    }

    public void checkIfAdmin(ApplicationUser user) {
        if (!isAdmin(user)) {
            throw new SecurityException("User is not admin");
        }
    }

    public boolean isAdmin() {
        return isAdmin(authenticationContext.getLoggedInUser());
    }

    public boolean isAdmin(ApplicationUser user) {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user);
    }
}
