package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDetails;
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
    private GroupManager groupManager;

    @ComponentImport
    @Inject
    private JiraAuthenticationContext authenticationContext;

    public ApplicationUser getAdmin() {
        return userManager.getUserByName("admin");
    }

    public ApplicationUser getUser() throws Exception {
        return getUser("user");
    }

    public ApplicationUser getUser(String name) throws Exception {
        ApplicationUser user = userManager.getUserByName(name);

        if (user == null) {
            user = userManager.createUser(new UserDetails(name, name).withPassword(name));
            groupManager.addUserToGroup(user, groupManager.getGroup("jira-software-users"));
        }

        return user;
    }

    @Deprecated
    public void asAdmin() {
        authenticationContext.setLoggedInUser(getAdmin());
    }

    public <T> T runAsUser(ApplicationUser user, Supplier<T> function) throws Exception {
        ApplicationUser oldUser = authenticationContext.getLoggedInUser();
        try {
            authenticationContext.setLoggedInUser(user);
            return function.get();
        } finally {
            authenticationContext.setLoggedInUser(oldUser);
        }
    }

    @FunctionalInterface
    public interface Supplier<T> {
        T get() throws Exception;
    }
}
