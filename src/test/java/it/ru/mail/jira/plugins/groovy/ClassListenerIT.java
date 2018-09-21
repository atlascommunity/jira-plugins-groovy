package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.event.project.VersionCreateEvent;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
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
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(Arquillian.class)
public class ClassListenerIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/classNameListenerEvent"
    );

    @Inject
    @ComponentImport
    private EventListenerRepository eventListenerRepository;
    @Inject
    @ComponentImport
    private CommentManager commentManager;
    @Inject
    @ComponentImport
    private VersionManager versionManager;
    @Inject
    @ComponentImport
    private ProjectManager projectManager;

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

        String script = FileUtil.readArquillianExample("tests/classNameListenerEvent");

        EventListenerForm form = new EventListenerForm();
        form.setName("test className listener");
        form.setScriptBody(script);

        ConditionDescriptor condition = new ConditionDescriptor();
        condition.setType(ConditionType.CLASS_NAME);
        condition.setClassName(VersionCreateEvent.class.getCanonicalName());
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
        assertNotEquals("updatedProject", projectManager.getProjectObj(project.getId()).getName());

        ApplicationUser user = userHelper.getUser();

        userHelper.runAsUser(
            user,
            () -> versionManager.createVersion("test version", null, null, null, project.getId(), null, false)
        );

        assertEquals("updatedProject", projectManager.getProjectObj(project.getId()).getName());
    }
}
