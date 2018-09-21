package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
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
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionDescriptor;
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionType;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerDto;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.api.repository.EventListenerRepository;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class IssueListenerIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/listener"
    );

    @Inject
    @ComponentImport
    private EventListenerRepository eventListenerRepository;
    @Inject
    @ComponentImport
    private CommentManager commentManager;

    @Inject
    private UserHelper userHelper;
    @Inject
    private ProjectHelper projectHelper;
    @Inject
    private IssueHelper issueHelper;

    private Integer listenerId;
    private Project project;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @Before
    public void beforeEach() throws Exception {
        ApplicationUser admin = userHelper.getAdmin();

        String script = FileUtil.readArquillianExample("tests/listener");

        EventListenerForm form = new EventListenerForm();
        form.setName("test listener");
        form.setScriptBody(script);

        ConditionDescriptor condition = new ConditionDescriptor();
        condition.setType(ConditionType.ISSUE);
        condition.setTypeIds(ImmutableSet.of(EventType.ISSUE_CREATED_ID));
        form.setCondition(condition);

        EventListenerDto listener = eventListenerRepository.createEventListener(admin, form);

        this.listenerId = listener.getId();
        this.project = userHelper.runAsUser(admin, () -> projectHelper.createProject(admin));
    }

    @After
    public void afterEach() {
        ApplicationUser admin = userHelper.getAdmin();

        if (project != null) {
            projectHelper.deleteProject(admin, project.getKey());
        }

        if (listenerId != null) {
            eventListenerRepository.deleteEventListener(admin, listenerId);
        }
    }

    @Test
    public void basicListenerShouldWork() throws Exception {
        ApplicationUser user = userHelper.getUser();

        Issue issue = userHelper.runAsUser(user, () -> issueHelper.createIssue(user, project));

        List<Comment> comments = commentManager.getComments(issue);

        assertNotNull(comments);
        assertEquals(comments.size(), 1);

        Comment comment = comments.get(0);

        assertEquals("test comment " + EventType.ISSUE_CREATED_ID, comment.getBody());
        assertEquals(user, comment.getAuthorApplicationUser());
    }
}
