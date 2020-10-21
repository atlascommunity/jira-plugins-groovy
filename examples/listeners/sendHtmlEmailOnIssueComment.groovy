import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.mail.Email
import groovy.xml.MarkupBuilder;

/*
* This script will send custom html email when a user from 'superuser' group
* comments an issue
*/

// Get issue on which user has commented
def issue = (MutableIssue) event.issue;
def commentManager = ComponentAccessor.getCommentManager()

// Get last comment
def comment = commentManager.getLastComment(issue)

// Get current jira instance url. It will be added to email template
def baseurl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")

// Get commenter user groups
def groupManager = ComponentAccessor.getGroupManager();
def groups = groupManager.getGroupsForUser(comment.getAuthorApplicationUser())

// Check if user is in 'superuser' group
if (groups.any {g -> g.getName().startsWith('superuser')}) {

    // Create html email
    def writer = new StringWriter()
    def html = new MarkupBuilder(writer)

    html.html {
        body(id: 'mainBody') {
            p("User: ${comment.getAuthorApplicationUser().getUsername()}")
            p("Comment: ${comment.body}")
            br()
            a(href: "$baseurl/browse/$issue.key", "${issue.getKey()}")
        }
    }

    String finalHTML = writer.toString()
    sendEmail('user.email@test.com', 'New comment on ' + issue.getKey(), finalHTML);
}

def sendEmail(String emailAddr, String subject, String body) {
    def mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer();
    Email email = new Email(emailAddr)
    email.setSubject(subject)
    email.setMimeType("text/html")
    email.setBody(body)
    mailServer.send(email)
}
