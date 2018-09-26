package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.workflow.WorkflowSchemeService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.*;
import com.atlassian.jira.workflow.migration.AssignableWorkflowSchemeMigrationHelper;
import com.atlassian.jira.workflow.migration.MigrationHelperFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.IssueHelper;
import it.ru.mail.jira.plugins.groovy.util.ProjectHelper;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class InlineWorkflowIT {
    @Inject
    @ComponentImport
    private WorkflowManager workflowManager;
    @Inject
    @ComponentImport
    private WorkflowSchemeManager workflowSchemeManager;
    @Inject
    @ComponentImport
    private WorkflowSchemeService workflowSchemeService;
    @Inject
    @ComponentImport
    private MigrationHelperFactory migrationHelperFactory;
    @Inject
    @ComponentImport
    private IssueService issueService;
    @Inject
    @ComponentImport
    private IssueManager issueManager;
    @Inject
    @ComponentImport
    private CommentManager commentManager;

    @Inject
    private ProjectHelper projectHelper;
    @Inject
    private UserHelper userHelper;
    @Inject
    private IssueHelper issueHelper;

    private String workflowName;
    private Project project;


    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil
            .prepareArchive(archive, ImmutableSet.of())
            .addAsResource("test_workflow.xml");
    }

    @Before
    public void beforeEach() throws Exception {
        ApplicationUser admin = userHelper.getAdmin();

        this.workflowName = "testwf" + System.currentTimeMillis();
        String workflowXml = FileUtil.readArquillianFile("test_workflow.xml");

        WorkflowDescriptor workflowDescriptor = WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXml);
        workflowDescriptor.validate();

        this.project = userHelper.runAsUser(admin, () -> projectHelper.createProject(admin));

        workflowManager.createWorkflow(admin, new ConfigurableJiraWorkflow(
            workflowName, workflowDescriptor, workflowManager
        ));

        AssignableWorkflowScheme.Builder assignableWorkflowSchemeBuilder = workflowSchemeManager.assignableBuilder();
        assignableWorkflowSchemeBuilder.setName(workflowName);
        assignableWorkflowSchemeBuilder.setDefaultWorkflow(workflowName);
        ServiceOutcome<AssignableWorkflowScheme> createWorkflowSchemeResult = workflowSchemeService.createScheme(admin, assignableWorkflowSchemeBuilder.build());
        if (!createWorkflowSchemeResult.isValid()) {
            fail(createWorkflowSchemeResult.getErrorCollection().toString());
        }
        AssignableWorkflowScheme workflowScheme = createWorkflowSchemeResult.getReturnedValue();

        AssignableWorkflowSchemeMigrationHelper migrationHelper = migrationHelperFactory.createMigrationHelper(project, workflowScheme);
        migrationHelper.associateProjectAndWorkflowScheme();
    }

    @After
    public void afterEach() {
        ApplicationUser admin = userHelper.getAdmin();

        if (project != null) {
            projectHelper.deleteProject(admin, project.getKey());
        }
    }

    @Test
    public void test() throws Exception {
        ApplicationUser admin = userHelper.getAdmin();
        ApplicationUser user = userHelper.getUser();

        Issue issue = userHelper.runAsUser(admin, () -> issueHelper.createIssue(admin, project));

        //assert inital state
        assertEquals(workflowName, workflowManager.getWorkflow(issue).getName());
        assertNull(issueManager.getIssueObject(issue.getId()).getAssignee());

        //test condition `currentUser.name == 'user'` and post function `issue.assignee = currentUser`
        IssueService.TransitionValidationResult validationResult = issueService.validateTransition(user, issue.getId(), 11, new IssueInputParametersImpl());
        assertResult(validationResult);
        IssueService.IssueResult transitionResult = issueService.transition(user, validationResult);
        assertResult(transitionResult);
        assertEquals(user, issueManager.getIssueObject(issue.getId()).getAssignee());

        validationResult = issueService.validateTransition(admin, issue.getId(), 11, new IssueInputParametersImpl());
        assertFalse(validationResult.isValid());

        //test validator "comment is not empty" and post function `comment = "test: " + comment`
        validationResult = issueService.validateTransition(user, issue.getId(), 21, new IssueInputParametersImpl());
        assertResult(validationResult);
        transitionResult = issueService.transition(user, validationResult);
        assertFalse(transitionResult.isValid());
        assertEquals(ImmutableMap.of("comment", "Comment is required"), transitionResult.getErrorCollection().getErrors());

        validationResult = userHelper.runAsUser(
            user,
            () -> issueService.validateTransition(
                user, issue.getId(), 21,
                new IssueInputParametersImpl().setComment("comment")
            )
        );
        assertResult(validationResult);

        transitionResult = issueService.transition(user, validationResult);
        assertResult(transitionResult);

        MutableIssue updatedIssue = issueManager.getIssueObject(issue.getId());

        List<Comment> comments = commentManager.getComments(updatedIssue);
        assertEquals(1, comments.size());
        Comment comment = comments.get(0);
        assertEquals("test: comment", comment.getBody());
        assertEquals(user, comment.getAuthorApplicationUser());

        assertEquals("10001", updatedIssue.getStatusId());
    }

    private static void assertResult(IssueService.TransitionValidationResult validationResult) {
        assertTrue(validationResult.getErrorCollection().toString(), validationResult.isValid());
    }

    private static void assertResult(IssueService.IssueResult result) {
        assertTrue(result.getErrorCollection().toString(), result.isValid());
    }
}
