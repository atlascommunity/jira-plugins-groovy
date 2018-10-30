import com.atlassian.jira.security.JiraAuthenticationContext
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.StandardModule

class WithStandardModule {
    private final JiraAuthenticationContext authenticationContext;

    WithStandardModule(@StandardModule JiraAuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    ApplicationUser getCu() {
        return authenticationContext.loggedInUser
    }
}
