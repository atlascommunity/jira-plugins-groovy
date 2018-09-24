package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionForm;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;
import ru.mail.jira.plugins.groovy.api.service.JqlFunctionService;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class JqlModulesIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/ScriptedFunction"
    );

    private static final Set<String> checkFunctions = ImmutableSet.of(
        "addedAfterSprintStart",
        "removedAfterSprintStart",

        "hasComments",
        "commented",
        "lastComment",

        "workLogged",

        "dateCompare",
        "issueFieldMatch",

        "hasLinks",
        "hasLinkType",
        "linkedIssuesOf",

        "parentsOf",
        "hasSubTasks",
        "subTasksOf",

        "epicsOf",
        "issuesInEpics"

    );

    @Inject
    @ComponentImport
    private JqlFunctionService jqlFunctionService;

    private JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry;

    @Inject
    private UserHelper userHelper;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @Before
    public void beforeEach() {
        jqlFunctionHandlerRegistry = ComponentAccessor.getComponent(JqlFunctionHandlerRegistry.class);

        assertNotNull(jqlFunctionHandlerRegistry);
    }

    @Test
    public void builtInFunctionsShouldBeRegistered() {
        List<String> allFunctionNames = jqlFunctionHandlerRegistry.getAllFunctionNames();

        for (String functionName : checkFunctions) {
            assertTrue(functionName + " is not present", allFunctionNames.contains("my_" + functionName));
        }
    }

    @Test
    public void functionShouldRegisterAndUnregister() throws Exception {
        ApplicationUser admin = userHelper.getAdmin();

        String scriptBody = FileUtil.readArquillianExample("tests/ScriptedFunction");

        JqlFunctionForm form = new JqlFunctionForm();
        form.setName("testListener" + System.currentTimeMillis());
        form.setScriptBody(scriptBody);

        JqlFunctionScriptDto script = jqlFunctionService.createScript(admin, form);
        assertTrue(jqlFunctionHandlerRegistry.getAllFunctionNames().contains(script.getName()));

        jqlFunctionService.deleteScript(admin, script.getId());
        assertFalse(jqlFunctionHandlerRegistry.getAllFunctionNames().contains(script.getName()));
    }
}
