import com.atlassian.jira.issue.Issue

import java.util.concurrent.TimeUnit

Issue issue = issue

return issue.created.time - TimeUnit.MINUTES.toMillis(10L)
