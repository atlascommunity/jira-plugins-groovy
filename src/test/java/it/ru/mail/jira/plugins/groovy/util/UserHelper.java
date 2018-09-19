package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class UserHelper {
    @ComponentImport
    @Inject
    private UserManager userManager;
    @ComponentImport
    @Inject
    private JiraAuthenticationContext authenticationContext;

    public ApplicationUser getAdmin() {
        return userManager.getUserByName("admin");
    }

    public void asAdmin() {
        authenticationContext.setLoggedInUser(getAdmin());
    }
}
