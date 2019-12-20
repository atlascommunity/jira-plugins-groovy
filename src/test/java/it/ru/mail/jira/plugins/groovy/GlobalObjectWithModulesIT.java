package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.Forms;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dao.GlobalObjectDao;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectDto;
import ru.mail.jira.plugins.groovy.api.e.UnableToLoadPluginException;
import ru.mail.jira.plugins.groovy.api.entity.GlobalObject;
import ru.mail.jira.plugins.groovy.api.repository.GlobalObjectRepository;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.service.TestHelperService;
import ru.mail.jira.plugins.groovy.api.service.ScriptInvalidationService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class GlobalObjectWithModulesIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/go/WithStandardModule",
        "tests/go/WithJswModule",
        "tests/go/WithPluginModule",
        "tests/go/NonExistingImport"
    );

    @Inject
    @ComponentImport
    private ScriptService scriptService;
    @Inject
    @ComponentImport
    private GlobalObjectRepository globalObjectRepository;
    @Inject
    @ComponentImport
    private GlobalObjectDao globalObjectDao;
    @Inject
    @ComponentImport
    private TestHelperService testHelperService;
    @Inject
    @ComponentImport("GlobalObjectsBindingProvider")
    private BindingProvider bindingProvider;
    @Inject
    @ComponentImport
    private ScriptInvalidationService scriptInvalidationService;
    @Inject
    private UserHelper userHelper;

    private String globalObjectName;
    private Set<Integer> deleteIds = new HashSet<>();

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @After
    public void afterEach() {
        for (Integer deleteId : deleteIds) {
            try {
                globalObjectRepository.delete(userHelper.getAdmin(), deleteId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createObject(String sourcePath) throws IOException {
        GlobalObjectDto globalObjectDto = globalObjectRepository.create(userHelper.getAdmin(), Forms.globalObject(sourcePath));

        globalObjectName = globalObjectDto.getName();
        deleteIds.add(globalObjectDto.getId());
    }

    @Test
    public void shouldWork() throws Exception {
        createObject("tests/go/WithStandardModule");

        ApplicationUser user = userHelper.getUser();

        Object result = userHelper.runAsUser(
            user,
            () -> scriptService.executeScript(null, globalObjectName + ".getCu()", ScriptType.CONSOLE, ImmutableMap.of())
        );

        assertEquals(user, result);
    }

    @Test
    public void staticShouldWork() throws Exception {
        createObject("tests/go/WithStandardModule");

        ApplicationUser user = userHelper.getUser();

        Object result = userHelper.runAsUser(
            user,
            () -> scriptService.executeScriptStatic(null, globalObjectName + ".getCu()", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of())
        );

        assertEquals(user, result);
    }

    @Test
    public void jswShouldWork() throws Exception {
        createObject("tests/go/WithJswModule");

        Object result = scriptService.executeScript(null, globalObjectName + ".getRankField()", ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void jswStaticShouldWork() throws Exception {
        createObject("tests/go/WithJswModule");

        Object result = scriptService.executeScriptStatic(null, globalObjectName + ".getRankField()", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void pluginShouldWork() throws Exception {
        createObject("tests/go/WithPluginModule");

        Object result = scriptService.executeScript(null, globalObjectName + ".getScripts()", ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void pluginStaticShouldWork() throws Exception {
        createObject("tests/go/WithPluginModule");

        Object result = scriptService.executeScriptStatic(null, globalObjectName + ".getScripts()", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void nonExistingShouldFail() throws Exception {
        boolean exceptionThrown = false;

        try {
            createObject("tests/go/NonExistingImport");
        } catch (Exception e) {
            Exception cause = testHelperService.getCompilationExceptionCause(e);

            if (cause instanceof UnableToLoadPluginException) {
                exceptionThrown = true;
            } else {
                throw e;
            }
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void brokenScriptShouldNotBreakEverything() throws Exception {
        GlobalObject script = globalObjectDao.createScript(userHelper.getAdmin(), Forms.globalObject("tests/go/NonExistingImport"));
        globalObjectName = script.getName();
        deleteIds.add(script.getID());

        scriptInvalidationService.invalidateGlobalObjects();

        assertNotNull(globalObjectDao.get(script.getID()));
        assertNull(bindingProvider.getBindings().get(globalObjectName));

        GlobalObjectDto legitScript = globalObjectRepository.create(userHelper.getAdmin(), Forms.globalObject("tests/go/WithStandardModule"));
        deleteIds.add(legitScript.getId());

        assertNotNull(globalObjectDao.get(script.getID()));
        assertNull(bindingProvider.getBindings().get(globalObjectName));

        assertNotNull(bindingProvider.getBindings().get(legitScript.getName()));
    }
}
