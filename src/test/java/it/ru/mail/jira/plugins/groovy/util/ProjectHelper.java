package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.template.ProjectTemplate;
import com.atlassian.jira.project.template.ProjectTemplateManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ProjectHelper {
    @ComponentImport
    @Inject
    private ProjectService projectService;

    @ComponentImport
    @Inject
    private ProjectTemplateManager projectTemplateManager;

    public Project createProject(ApplicationUser user) {
        ProjectTemplate projectTemplate = projectTemplateManager.getDefaultTemplate();

        ProjectService.CreateProjectValidationResult projectValidationResult = projectService.validateCreateProject(
            user,
            new ProjectCreationData.Builder()
                .withKey("TESTPROJ")
                .withName("TESTPROJ")
                .withLead(user)
                .withType(projectTemplate.getProjectTypeKey())
                .withProjectTemplateKey(projectTemplate.getKey().getKey())
                .build()
        );
        return projectService.createProject(projectValidationResult);
    }

    public void deleteProject(ApplicationUser user, String key) {
        ProjectService.DeleteProjectValidationResult validationResult = projectService.validateDeleteProject(user, key);

        projectService.deleteProject(user, validationResult);
    }
}
