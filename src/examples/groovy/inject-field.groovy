import com.atlassian.jira.security.JiraAuthenticationContext
import com.atlassian.jira.user.ApplicationUser
import groovy.transform.Field
import ru.mail.jira.plugins.groovy.api.script.StandardModule

@StandardModule @Field
JiraAuthenticationContext authenticationContext

return getCu()

ApplicationUser getCu() {
    return authenticationContext.loggedInUser
}
