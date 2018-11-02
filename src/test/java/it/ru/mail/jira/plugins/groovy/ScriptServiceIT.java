package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.dto.docs.MethodDoc;
import ru.mail.jira.plugins.groovy.api.dto.docs.ParameterDoc;
import ru.mail.jira.plugins.groovy.api.dto.docs.TypeDoc;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.GroovyDocService;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;

import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ScriptServiceIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/jsonSlurper",
        "tests/standardModule",
        "tests/pluginModule",
        "tests/containerService",
        "tests/GroovyDocTest"
    );

    @ComponentImport
    @Inject
    private ScriptService scriptService;

    @ComponentImport
    @Inject
    private GroovyDocService groovyDocService;

    @ComponentImport
    @Inject
    private JiraAuthenticationContext authenticationContext;

    @ComponentImport
    @Inject
    private UserManager userManager;

    @ComponentImport
    @Inject
    private BuildUtilsInfo buildUtilsInfo;

    private ApplicationUser oldUser;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
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

    @Test
    public void pluginModuleShouldWorkWithPrivateService() throws Exception {
        String script = FileUtil.readArquillianExample("tests/containerService");

        Object result = scriptService.executeScript(null, script, ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void docGenerationShouldWork() throws Exception {
        ClassDoc generatedDoc = groovyDocService.parseDocs(FileUtil.readArquillianExample("tests/GroovyDocTest"));

        assertEquals(
            new ClassDoc(
                "some description",
                ImmutableList.of(
                    new MethodDoc(
                        "getUserByName",
                        "Returns user for current name<DL><DT><B>name:</B></DT><DD>user name</DD></DL>",
                        new TypeDoc(
                            "com.atlassian.jira.user.ApplicationUser",
                            buildLink(
                                "https://docs.atlassian.com/software/jira/docs/api/" + buildUtilsInfo.getVersion() + "/com/atlassian/jira/user/ApplicationUser.html",
                                "com.atlassian.jira.user.ApplicationUser"
                            )
                        ),
                        ImmutableList.of(
                            new ParameterDoc(
                                new TypeDoc(
                                    "java.lang.String",
                                    buildLink(
                                        "https://docs.oracle.com/javase/8/docs/api/java/lang/String.html",
                                        "java.lang.String"
                                    )
                                ),
                                "name"
                            )
                        )
                    ),
                    new MethodDoc(
                        "voidMethod",
                        null,
                        new TypeDoc("void", "void"),
                        ImmutableList.of()
                    )
                )
            ),
            generatedDoc
        );
    }

    private static String buildLink(String url, String className) {
        return "<a href='" + url + "' title='" + className + "'>" + className + "</a>";
    }
}
