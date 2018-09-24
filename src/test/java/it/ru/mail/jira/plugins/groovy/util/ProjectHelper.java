package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.template.ProjectTemplate;
import com.atlassian.jira.project.template.ProjectTemplateManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ProjectHelper {
    @ComponentImport
    @Inject
    private ProjectService projectService;

    @ComponentImport
    @Inject
    private ProjectRoleManager projectRoleManager;

    @ComponentImport
    @Inject
    private ProjectRoleService projectRoleService;

    @ComponentImport
    @Inject
    private ProjectTemplateManager projectTemplateManager;

    @Inject
    private UserHelper userHelper;

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

    public void addToAdmins(Project project, ApplicationUser user) throws Exception {
        ProjectRole adminRole = projectRoleManager.getProjectRole("Administrators");

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        userHelper.runAsUser(userHelper.getAdmin(), () -> {
            projectRoleService.addActorsToProjectRole(
                ImmutableList.of(user.getKey()),
                adminRole,
                project,
                ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                errorCollection
            );

            return null;
        });

        if (errorCollection.hasAnyErrors()) {
            Assert.fail(errorCollection.toString());
        }
    }

    public void deleteProject(ApplicationUser user, String key) {
        ProjectService.DeleteProjectValidationResult validationResult = projectService.validateDeleteProject(user, key);

        projectService.deleteProject(user, validationResult);
    }
}
