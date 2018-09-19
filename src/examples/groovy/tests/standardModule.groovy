import com.atlassian.jira.security.JiraAuthenticationContext
import ru.mail.jira.plugins.groovy.api.script.StandardModule

@StandardModule
JiraAuthenticationContext authenticationContext

return authenticationContext.loggedInUser
