package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectDto;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.api.e.UnableToLoadPluginException;
import ru.mail.jira.plugins.groovy.api.repository.GlobalObjectRepository;
import ru.mail.jira.plugins.groovy.api.script.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.service.TestHelperService;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class GlobalObjectWithModulesIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/go/WithStandardModule",
        "tests/go/WithJswModule",
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
    private TestHelperService testHelperService;
    @Inject
    @ComponentImport("GlobalObjectsBindingProvider")
    private BindingProvider bindingProvider;
    @Inject
    private UserHelper userHelper;

    private String globalObjectName;
    private Integer globalObjectId;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @After
    public void afterEach() {
        if (globalObjectId != null) {
            globalObjectRepository.delete(userHelper.getAdmin(), globalObjectId);
        }
    }

    private void createObject(String sourcePath) throws IOException {
        long ts = System.currentTimeMillis();
        globalObjectName = "testObject" + ts;

        GlobalObjectForm form = new GlobalObjectForm();
        form.setName(globalObjectName);
        form.setScriptBody(FileUtil.readArquillianExample(sourcePath));

        GlobalObjectDto globalObjectDto = globalObjectRepository.create(userHelper.getAdmin(), form);

        globalObjectId = globalObjectDto.getId();
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
    public void pluginShouldWork() throws Exception {
        createObject("tests/go/WithJswModule");

        Object result = scriptService.executeScript(null, globalObjectName + ".getRankField()", ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void pluginStaticShouldWork() throws Exception {
        createObject("tests/go/WithJswModule");

        Object result = scriptService.executeScriptStatic(null, globalObjectName + ".getRankField()", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());

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
}
