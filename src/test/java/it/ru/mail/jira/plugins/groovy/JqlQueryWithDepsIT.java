package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.google.common.collect.ImmutableList;
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
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionForm;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;
import ru.mail.jira.plugins.groovy.api.service.JqlFunctionService;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class JqlQueryWithDepsIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/ScriptedFunction"
    );

    @Inject
    @ComponentImport
    private JqlFunctionService jqlFunctionService;
    @Inject
    @ComponentImport
    private SearchService searchService;

    @Inject
    private ProjectHelper projectHelper;
    @Inject
    private UserHelper userHelper;
    @Inject
    private IssueHelper issueHelper;

    private JqlFunctionScriptDto script;
    private Project project;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @Before
    public void beforeEach() throws Exception {
        ApplicationUser admin = userHelper.getAdmin();

        String script = FileUtil.readArquillianExample("tests/ScriptedFunction");

        JqlFunctionForm form = new JqlFunctionForm();
        form.setName("testListener" + System.currentTimeMillis());
        form.setScriptBody(script);

        this.script = jqlFunctionService.createScript(admin, form);
        this.project = userHelper.runAsUser(admin, () -> projectHelper.createProject(admin));
    }

    @After
    public void afterEach() {
        ApplicationUser admin = userHelper.getAdmin();

        if (project != null) {
            projectHelper.deleteProject(admin, project.getKey());
        }

        if (script != null) {
            jqlFunctionService.deleteScript(admin, script.getId());
        }
    }

    @Test
    public void functionShouldWork() throws Exception {
        ApplicationUser user = userHelper.getUser();
        ApplicationUser admin = userHelper.getAdmin();

        projectHelper.addToAdmins(project, user);

        Issue assignedToUser = issueHelper.createIssue(user, project, issue -> issue.setAssignee(user));
        Issue assignedToAdmin = issueHelper.createIssue(user, project, issue -> issue.setAssignee(admin));
        issueHelper.createIssue(user, project, issue -> issue.setAssignee(null));

        Query projectQuery = JqlQueryBuilder
            .newBuilder()
            .where()
            .project(this.project.getId())
            .buildQuery();
        Query query = JqlQueryBuilder
            .newBuilder(projectQuery)
            .where()
            .and()
            .field("groovyFunction")
            .in()
            .function(script.getName())
            .buildQuery();

        long projectIssueCount = searchService.searchCountOverrideSecurity(user, projectQuery);

        assertEquals(3, projectIssueCount);

        assertFalse(searchService.validateQuery(user, query).hasAnyErrors());
        SearchResults userResults = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
        assertEquals(ImmutableList.of(assignedToUser), userResults.getIssues());

        assertFalse(searchService.validateQuery(admin, query).hasAnyErrors());
        SearchResults adminResults = searchService.search(admin, query, PagerFilter.getUnlimitedFilter());
        assertEquals(ImmutableList.of(assignedToAdmin), adminResults.getIssues());
    }

    @Test
    public void validationShouldWork() throws Exception {
        ApplicationUser user = userHelper.getUser("kek");

        Query query = JqlQueryBuilder
            .newBuilder()
            .where()
            .field("groovyFunction")
            .in()
            .function(script.getName())
            .buildQuery();

        MessageSet validationResult = searchService.validateQuery(user, query);

        assertTrue(validationResult.hasAnyErrors());
        assertEquals(ImmutableSet.of("user is kek"), validationResult.getErrorMessages());
    }
}
