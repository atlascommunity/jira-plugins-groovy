package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionHelper {
    private final GlobalPermissionManager globalPermissionManager;

    @Autowired
    public PermissionHelper(
        @ComponentImport GlobalPermissionManager globalPermissionManager
    ) {
        this.globalPermissionManager = globalPermissionManager;
    }

    public void checkIfAdmin(ApplicationUser user) {
        if (!globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user)) {
            throw new SecurityException("User is not admin");
        };
    }
}
