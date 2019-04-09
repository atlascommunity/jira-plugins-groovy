package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.*;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ScriptFieldIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/scriptField"
    );

    @Inject
    private ProjectHelper projectHelper;
    @Inject
    private CustomFieldHelper fieldHelper;
    @Inject
    private IssueHelper issueHelper;
    @Inject
    private UserHelper userHelper;

    @ComponentImport
    @Inject
    private FieldConfigRepository fieldConfigRepository;

    @ComponentImport
    @Inject
    private IssueManager issueManager;

    private Project project;

    private CustomField field;
    private Issue issue;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @Before
    public void beforeEach() {
        userHelper.asAdmin();
        ApplicationUser admin = userHelper.getAdmin();

        this.project = projectHelper.createProject(admin);
    }

    @After
    public void afterEach() throws RemoveException {
        ApplicationUser admin = userHelper.getAdmin();

        if (project != null) {
            projectHelper.deleteProject(admin, project.getKey());
        }
        if (field != null) {
            fieldHelper.deleteField(field);
        }
    }

    @Test
    public void scriptedFieldShouldWork() throws Exception {
        this.field = fieldHelper.createNumberField();
        this.issue = issueHelper.createIssue(userHelper.getAdmin(), project);

        FieldConfig fieldConfig = fieldHelper.getFirstConfig(field);

        assertNotNull(fieldConfig);

        String script = FileUtil.readArquillianExample("tests/scriptField");

        FieldConfigForm form = new FieldConfigForm();
        form.setCacheable(true);
        form.setScriptBody(script);
        form.setVelocityParamsEnabled(false);

        fieldConfigRepository.updateConfig(userHelper.getAdmin(), fieldConfig.getId(), form);

        MutableIssue issue = issueManager.getIssueObject(this.issue.getId());

        assertEquals(
            (double) (issue.getCreated().getTime() - TimeUnit.MINUTES.toMillis(10L)),
            issue.getCustomFieldValue(field)
        );
    }

    private boolean isIssueReIndexed(ApplicationUser expectedAssignee) throws Exception {
        return issueHelper.search(
            userHelper.getAdmin(),
            JqlQueryBuilder
                .newBuilder()
                .where()
                .field("key").eq(issue.getKey())
                .and()
                .assignee().eq(expectedAssignee.getName())
                .buildQuery()
        ).getTotal() == 1;
    }

    @Test
    public void newValueShouldBeIndexedAfterIssueUpdate() throws Exception {
        this.field = fieldHelper.createUserField();
        this.issue = issueHelper.createIssue(userHelper.getAdmin(), project);

        issueHelper.assignIssue(issueHelper.getIssue(this.issue.getKey()), userHelper.getAdmin(), userHelper.getAdmin());

        FieldConfig fieldConfig = fieldHelper.getFirstConfig(field);

        assertNotNull(fieldConfig);

        FieldConfigForm form = new FieldConfigForm();
        form.setCacheable(true);
        form.setScriptBody("return issue.assignee");
        form.setVelocityParamsEnabled(false);

        fieldConfigRepository.updateConfig(userHelper.getAdmin(), fieldConfig.getId(), form);

        MutableIssue issue = issueManager.getIssueObject(this.issue.getId());

        assertEquals(
            userHelper.getAdmin(),
            issueHelper.getIssue(issue.getKey()).getCustomFieldValue(field)
        );

        Thread.sleep(1200); //wait more than one second to have different update date

        issueHelper.assignIssue(issueHelper.getIssue(this.issue.getKey()), userHelper.getAdmin(), userHelper.getUser());

        while (true) {
            if (isIssueReIndexed(userHelper.getUser())) break; //make sure that issue is re-indexed
        }

        assertEquals(
            0,
            issueHelper.search(
                userHelper.getAdmin(),
                JqlQueryBuilder
                    .newBuilder()
                    .where()
                    .field("key").eq(this.issue.getKey())
                    .and()
                    .customField(field.getIdAsLong()).eq(userHelper.getAdmin().getName())
                    .buildQuery()
            ).getTotal()
        );
        assertEquals(
            1,
            issueHelper.search(
                userHelper.getAdmin(),
                JqlQueryBuilder
                    .newBuilder()
                    .where()
                    .field("key").eq(this.issue.getKey())
                    .and()
                    .customField(field.getIdAsLong()).eq("user")
                    .buildQuery()
            ).getTotal()
        );
        assertEquals(
            userHelper.getUser(),
            issueHelper.getIssueFromIndex(userHelper.getAdmin(), this.issue.getKey()).getCustomFieldValue(field)
        );
        assertEquals(
            userHelper.getUser(),
            issueHelper.getIssue(this.issue.getKey()).getCustomFieldValue(field)
        );
    }
}
