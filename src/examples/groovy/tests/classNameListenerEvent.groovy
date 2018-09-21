import com.atlassian.jira.event.project.VersionCreateEvent
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.project.UpdateProjectParameters
import com.atlassian.jira.project.version.Version
import ru.mail.jira.plugins.groovy.api.script.StandardModule

@StandardModule
ProjectManager projectManager

VersionCreateEvent event = event

Version version = event.version

projectManager.updateProject(
    UpdateProjectParameters
        .forProject(version.projectId)
        .name("updatedProject")
)
