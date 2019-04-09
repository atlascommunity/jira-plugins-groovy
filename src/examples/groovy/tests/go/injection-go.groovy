import com.atlassian.jira.security.JiraAuthenticationContext
import ru.mail.jira.plugins.groovy.api.script.StandardModule

@StandardModule
JiraAuthenticationContext $goName

return $goName.loggedInUser.name
