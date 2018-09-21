import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.issue.comments.CommentManager

IssueEvent event = event

CommentManager commentManager = ComponentAccessor.commentManager

commentManager.create(event.issue, event.getUser(), "test comment ${event.eventTypeId}", true)
