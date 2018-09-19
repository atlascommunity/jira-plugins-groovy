package it.ru.mail.jira.plugins.groovy;

import com.adaptavist.shrinkwrap.atlassian.plugin.api.AtlassianPluginArchive;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.CustomFieldHelper;
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
import org.ofbiz.core.entity.GenericEntityException;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.nio.file.Paths;
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
    public static Archive<?> useSpringScannerOne(Archive<?> archive) {
        AtlassianPluginArchive result = archive.as(AtlassianPluginArchive.class);
        result
            .addClass(FileUtil.class)
            .addPackage("it.ru.mail.jira.plugins.groovy.util")
            .withSpringScannerOne(false);

        for (String script : requiredScripts) {
            result.addAsResource(Paths.get("src/examples/groovy/" + script + ".groovy").toFile(), script + ".groovy");
        }

        return result;
    }

    @Before
    public void beforeEach() throws GenericEntityException, CreateException {
        userHelper.asAdmin();
        ApplicationUser admin = userHelper.getAdmin();

        this.project = projectHelper.createProject(admin);
        this.field = fieldHelper.createNumberField();
        this.issue = issueHelper.createIssue(admin, project);
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
        FieldConfig fieldConfig = field.getConfigurationSchemes().iterator().next().getConfigs().values().iterator().next();

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
}
