package it.ru.mail.jira.plugins.groovy;

import com.adaptavist.shrinkwrap.atlassian.plugin.api.AtlassianPluginArchive;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;

import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ScriptServiceIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/jsonSlurper",
        "tests/standardModule",
        "tests/pluginModule"
    );

    @ComponentImport
    @Inject
    private ScriptService scriptService;

    @ComponentImport
    @Inject
    private JiraAuthenticationContext authenticationContext;

    @ComponentImport
    @Inject
    private UserManager userManager;

    private ApplicationUser oldUser;

    @BeforeDeployment
    public static Archive<?> useSpringScannerOne(Archive<?> archive) {
        AtlassianPluginArchive result = archive.as(AtlassianPluginArchive.class);
        result
            .addClass(FileUtil.class)
            .withSpringScannerOne(false);

        for (String script : requiredScripts) {
            result.addAsResource(Paths.get("src/examples/groovy/" + script + ".groovy").toFile(), script + ".groovy");
        }

        return result;
    }

    @Before
    public void beforeEach() {
        oldUser = authenticationContext.getLoggedInUser();

        authenticationContext.setLoggedInUser(userManager.getUserByName("admin"));
    }

    @After
    public void afterEach() {
        authenticationContext.setLoggedInUser(oldUser);
    }

    @Test
    public void basicScriptShouldRun() throws Exception {
        Object result = scriptService.executeScript(null, "return 'test'", ScriptType.CONSOLE, ImmutableMap.of());

        assertEquals(result, "test");
    }

    @Test
    public void jsonShouldBeParsed() throws Exception {
        String script = FileUtil.readArquillianExample("tests/jsonSlurper");

        Object result = scriptService.executeScript(null, script, ScriptType.CONSOLE, ImmutableMap.of());

        assertEquals(result, ImmutableMap.of("test", "value"));
    }

    @Test
    public void standardModuleShouldWork() throws Exception {
        String script = FileUtil.readArquillianExample("tests/standardModule");

        Object result = scriptService.executeScript(null, script, ScriptType.CONSOLE, ImmutableMap.of());

        assertEquals(result, userManager.getUserByName("admin"));
    }

    @Test
    public void pluginModuleShouldWork() throws Exception {
        String script = FileUtil.readArquillianExample("tests/pluginModule");

        Object result = scriptService.executeScript(null, script, ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);
    }
}
