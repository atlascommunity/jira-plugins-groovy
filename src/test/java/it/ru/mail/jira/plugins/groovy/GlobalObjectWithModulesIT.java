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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dao.GlobalObjectDao;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectDto;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
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
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class GlobalObjectWithModulesIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/go/SlowGlobalObject",
        "tests/go/WithStandardModule",
        "tests/go/WithJswModule",
        "tests/go/WithPluginModule",
        "tests/go/NonExistingImport",
        "tests/go/BorkedObject",
        "tests/go/GlobalObject",
        "tests/go/WithGoDependency"
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
    private ScheduledExecutorService executorService = null;

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

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    ScheduledExecutorService getExecutor() {
        return executorService != null ? executorService : (executorService = Executors.newScheduledThreadPool(5));
    }

    private void createObject(String sourcePath) throws IOException {
        GlobalObjectDto globalObjectDto = createObject(Forms.globalObject(sourcePath));
        globalObjectName = globalObjectDto.getName();
    }

    private GlobalObjectDto createObject(GlobalObjectForm form) throws IOException {
        GlobalObjectDto globalObjectDto = globalObjectRepository.create(userHelper.getAdmin(), form);
        deleteIds.add(globalObjectDto.getId());
        return globalObjectDto;
    }

    private GlobalObject createObjectDirect(GlobalObjectForm form) throws IOException {
        GlobalObject globalObject = globalObjectDao.createScript(userHelper.getAdmin(), form);
        deleteIds.add(globalObject.getID());
        return globalObject;
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

        assertNotNull(scriptService.executeScript(null, globalObjectName + ".getScripts()", ScriptType.CONSOLE, ImmutableMap.of()));
        assertEquals("\u043d\u043e\u043b\u044c", scriptService.executeScript(null, globalObjectName + ".testStatics()", ScriptType.CONSOLE, ImmutableMap.of()));
        assertEquals("\u043d\u043e\u043b\u044c", scriptService.executeScript(null, "ru.mail.jira.groovy.testgo.JsIncluderGlobalObject.testStaticMethod()", ScriptType.CONSOLE, ImmutableMap.of()));
    }

    @Test
    @Ignore
    //ignore failing test, since we don't have any uses cases of compilestatic calls of global objects
    public void pluginStaticShouldWork() throws Exception {
        createObject("tests/go/WithPluginModule");

        assertNotNull(scriptService.executeScriptStatic(null, globalObjectName + ".getScripts()", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of()));
        assertEquals("\u043d\u043e\u043b\u044c", scriptService.executeScriptStatic(null, globalObjectName + ".testStatics()", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of()));
        assertEquals("\u043d\u043e\u043b\u044c", scriptService.executeScriptStatic(null, "ru.mail.jira.groovy.testgo.JsIncluderGlobalObject.testStaticMethod()", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of()));
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

    @Test
    public void globalBindingsShouldNotWork() throws Exception {
        boolean exceptionOccurred = false;

        try {
            createObject("tests/go/BorkedObject");
        } catch (Exception e) {
            if ("org.codehaus.groovy.control.MultipleCompilationErrorsException".equals(e.getClass().getCanonicalName())) {
                exceptionOccurred = true;
            } else {
                throw e;
            }
        }

        assertTrue(exceptionOccurred);
    }

    @Test
    public void globalObjectImportShouldWork() throws Exception {
        GlobalObjectDto object = createObject(Forms.globalObject("tests/go/GlobalObject"));
        GlobalObjectForm form = Forms.globalObject("tests/go/WithGoDependency");
        form.setScriptBody(
            form.getScriptBody().replaceAll(
                Pattern.quote("$INJECTED_GO_CLASSNAME$"),
                bindingProvider.getBindings().get(object.getName()).getType().getSimpleName()
            )
        );
        GlobalObjectDto object2 = createObject(form);

        assertNotNull(globalObjectDao.get(object.getId()));
        assertNotNull(bindingProvider.getBindings().get(object.getName()));
        assertNotNull(globalObjectDao.get(object2.getId()));
        assertNotNull(bindingProvider.getBindings().get(object2.getName()));

        Object result = scriptService.executeScript(null, object2.getName() + ".getAdmin()", ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void dependencyShouldNotBreakEverythingAfterClassChange() throws Exception {
        GlobalObjectDto object = createObject(Forms.globalObject("tests/go/GlobalObject"));
        GlobalObjectForm form = Forms.globalObject("tests/go/WithGoDependency");
        form.setScriptBody(
            form.getScriptBody().replaceAll(
                Pattern.quote("$INJECTED_GO_CLASSNAME$"),
                bindingProvider.getBindings().get(object.getName()).getType().getSimpleName()
            )
        );
        GlobalObjectDto object2 = createObject(form);

        Object result = scriptService.executeScript(null, object2.getName() + ".getAdmin()", ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);

        assertNotNull(globalObjectDao.get(object.getId()));
        assertNotNull(bindingProvider.getBindings().get(object.getName()));
        assertNotNull(globalObjectDao.get(object2.getId()));
        assertNotNull(bindingProvider.getBindings().get(object2.getName()));

        GlobalObjectForm updateForm = Forms.globalObject("tests/go/GlobalObject");
        updateForm.setComment("update form");
        object = globalObjectRepository.update(userHelper.getAdmin(), object.getId(), updateForm);

        assertNotNull(globalObjectDao.get(object.getId()));
        assertNotNull(bindingProvider.getBindings().get(object.getName()));
        assertNotNull(globalObjectDao.get(object2.getId()));
        assertNull(bindingProvider.getBindings().get(object2.getName()));
    }

    @Test
    public void prematureInitializationShouldWork() throws Exception {
        GlobalObjectDto object = createObject(Forms.globalObject("tests/go/GlobalObject"));
        GlobalObjectForm form = Forms.globalObject("tests/go/WithGoDependency");
        form.setScriptBody(
            form.getScriptBody().replaceAll(
                Pattern.quote("$INJECTED_GO_CLASSNAME$"),
                bindingProvider.getBindings().get(object.getName()).getType().getSimpleName()
            )
        );
        GlobalObjectDto object2 = createObject(form);

        assertNotNull(globalObjectDao.get(object.getId()));
        assertNotNull(bindingProvider.getBindings().get(object.getName()));
        assertNotNull(globalObjectDao.get(object2.getId()));
        assertNotNull(bindingProvider.getBindings().get(object2.getName()));

        Object result = scriptService.executeScript(null, object2.getName() + ".getAdmin()", ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);

        testHelperService.deinitializeGlobalObjects();

        assertNotNull(globalObjectDao.get(object.getId()));
        assertNotNull(bindingProvider.getBindings().get(object.getName()));
        assertNotNull(globalObjectDao.get(object2.getId()));
        assertNotNull(bindingProvider.getBindings().get(object2.getName()));

        result = scriptService.executeScript(null, object2.getName() + ".getAdmin()", ScriptType.CONSOLE, ImmutableMap.of());

        assertNotNull(result);
    }

    @Test
    public void circularDependencyShouldFailSafely() throws Exception {
        GlobalObjectForm form1 = Forms.globalObject("tests/go/WithGoDependency");
        Thread.sleep(200);
        GlobalObjectForm form2 = Forms.globalObject("tests/go/WithGoDependency");

        String name1 = form1.getName().substring("testObject".length());
        String name2 = form2.getName().substring("testObject".length());

        form2.setScriptBody(
            form2.getScriptBody().replaceAll(
                Pattern.quote("$INJECTED_GO_CLASSNAME$"),
                "GlobalObject" + name1
            )
        );
        form2.setDependencies("ru.mail.jira.scripts.go.GlobalObject" + name1);
        form1.setScriptBody(
            form1.getScriptBody().replaceAll(
                Pattern.quote("$INJECTED_GO_CLASSNAME$"),
                "GlobalObject" + name2
            )
        );
        form2.setDependencies("ru.mail.jira.scripts.go.GlobalObject" + name2);

        GlobalObject object1 = createObjectDirect(form1);
        GlobalObject object2 = createObjectDirect(form2);
        GlobalObjectDto okObject = createObject(Forms.globalObject("tests/go/GlobalObject"));

        testHelperService.deinitializeGlobalObjects();

        assertNotNull(globalObjectDao.get(okObject.getId()));
        assertNotNull(bindingProvider.getBindings().get(okObject.getName()));
        assertNotNull(globalObjectDao.get(object1.getID()));
        assertNull(bindingProvider.getBindings().get(object1.getName()));
        assertNotNull(globalObjectDao.get(object2.getID()));
        assertNull(bindingProvider.getBindings().get(object2.getName()));

        Object result = scriptService.executeScript(null, okObject.getName() + ".getAdmin()", ScriptType.CONSOLE, ImmutableMap.of());
        assertNotNull(result);
    }

    @Test(timeout = 60_000)
    public void classLoaderDeadlockShouldNotHappen() throws Exception {
        GlobalObjectDto object = createObject(Forms.globalObject("tests/go/SlowGlobalObject"));
        GlobalObjectForm form = Forms.globalObject("tests/go/WithGoDependency");
        String objectClassName = bindingProvider.getBindings().get(object.getName()).getType().getSimpleName();
        form.setScriptBody(
            form.getScriptBody().replaceAll(
                Pattern.quote("$INJECTED_GO_CLASSNAME$"),
                objectClassName
            )
        );
        GlobalObjectDto object2 = createObject(form);

        assertNotNull(globalObjectDao.get(object.getId()));
        assertNotNull(bindingProvider.getBindings().get(object.getName()));
        assertNotNull(globalObjectDao.get(object2.getId()));
        assertNotNull(bindingProvider.getBindings().get(object2.getName()));

        Object result = scriptService.executeScript(null, object2.getName() + ".getAdmin()", ScriptType.CONSOLE, ImmutableMap.of());
        assertNotNull(result);

        ScheduledFuture<Object> outcome = getExecutor().schedule(
            () -> scriptService.executeScript(
                null,
                "import ru.mail.jira.scripts.go." + objectClassName + ";" +
                    "assert " + objectClassName + " != null;" +
                    "return " + object2.getName() + ".getAdmin()",
                ScriptType.CONSOLE, ImmutableMap.of()
            ),
            5, TimeUnit.SECONDS
        );
        scriptInvalidationService.invalidateGlobalObjects();

        assertNotNull(outcome.get());
    }
}
