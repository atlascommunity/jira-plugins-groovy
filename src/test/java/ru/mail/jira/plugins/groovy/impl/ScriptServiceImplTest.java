package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.GlobalFunctionManager;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.script.ParseContext;
import ru.mail.jira.plugins.groovy.util.cl.DelegatingClassLoader;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(JUnitPlatform.class)
class ScriptServiceImplTest {
    private ScriptService scriptService;

    @BeforeEach
    public void setup() {
        PluginEventManager pluginEventManager = new DefaultPluginEventManager();
        GlobalFunctionManager globalFunctionManager = new GlobalFunctionManagerImpl();
        DelegatingClassLoader delegatingClassLoader = new DelegatingClassLoader();
        MockPlugin testPlugin = new MockPlugin("Test plugin", "testPLugin", new PluginInformation(), PluginState.ENABLED);
        testPlugin.setClassLoader(Thread.currentThread().getContextClassLoader());
        InjectionResolver injectionResolver = new MockInjectionResolver(
            ImmutableMap.of(
                JiraAuthenticationContext.class, new MockSimpleAuthenticationContext(testUser())
            ),
            ImmutableMap.of(
                "testPlugin", testPlugin
            )
        );

        scriptService = new ScriptServiceImpl(
            pluginEventManager,
            injectionResolver,
            globalFunctionManager,
            delegatingClassLoader
        );
    }

    private static Stream<Arguments> createBooleanValues() {
        return Stream.of(
            Arguments.of(false),
            Arguments.of(true)
        );
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void basicTest(boolean isStatic) throws Exception {
        String script = "return 'ok'";

        Object result;
        if (isStatic) {
            result = scriptService.executeScriptStatic(null, script, ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());
        } else {
            result = scriptService.executeScript(null, script, ScriptType.CONSOLE, ImmutableMap.of());
        }

        assertEquals(result, "ok");
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void bindingTest(boolean isStatic) throws Exception {
        String script = FileUtil.readExample("tests/binding-test");

        Map<String, Object> bindings = ImmutableMap.of("user", testUser());

        Object result;
        if (isStatic) {
            ImmutableMap<String, Class> types = ImmutableMap.of(
                "user", ApplicationUser.class
            );
            result = scriptService.executeScriptStatic(null, script, ScriptType.CONSOLE, bindings, types);
        } else {
            result = scriptService.executeScript(null, script, ScriptType.CONSOLE, bindings);
        }

        assertEquals(result, "userName1337");
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void injectionTest(boolean isStatic) throws Exception {
        String script = FileUtil.readExample("inject-var");

        Object result;
        if (isStatic) {
            result = scriptService.executeScriptStatic(null, script, ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());
        } else {
            result = scriptService.executeScript(null, script, ScriptType.CONSOLE, ImmutableMap.of());
        }

        assertEquals(result, testUser());
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void fieldInjectionTest(boolean isStatic) throws Exception {
        String script = FileUtil.readExample("inject-field");

        Object result;
        if (isStatic) {
            result = scriptService.executeScriptStatic(null, script, ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());
        } else {
            result = scriptService.executeScript(null, script, ScriptType.CONSOLE, ImmutableMap.of());
        }

        assertEquals(result, testUser());
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void scriptParamsTest(boolean isStatic) throws Exception {
        String script = FileUtil.readExample("admin-param");

        Map<String, Object> bindings = ImmutableMap.of("user", testUser());

        Object result;
        if (isStatic) {
            ImmutableMap<String, Class> types = ImmutableMap.of(
                "user", ApplicationUser.class
            );
            result = scriptService.executeScriptStatic(null, script, ScriptType.ADMIN_SCRIPT, bindings, types);
        } else {
            result = scriptService.executeScript(null, script, ScriptType.ADMIN_SCRIPT, bindings);
        }

        assertEquals(result, "User Name 1337");
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void scriptParamsParseTest(boolean isStatic) throws Exception {
        String script = FileUtil.readExample("admin-param");

        ParseContext parseContext;
        if (isStatic) {
            ImmutableMap<String, Class> types = ImmutableMap.of(
                "user", ApplicationUser.class
            );
            parseContext = scriptService.parseScriptStatic(script, types);
        } else {
            parseContext = scriptService.parseScript(script);
        }

        assertEquals(
            parseContext.getParameters(),
            ImmutableList.of(
                new ScriptParamDto(
                    "user",
                    "User",
                    ParamType.USER,
                    false
                )
            )
        );
    }

    @Test
    public void nonExistingMethodStcTest() throws Exception {
        String script = FileUtil.readExample("tests/stc-error");

        ImmutableMap<String, Class> types = ImmutableMap.of(
            "user", ApplicationUser.class
        );

        assertThrows(
            MultipleCompilationErrorsException.class,
            () -> scriptService.parseScriptStatic(script, types)
        );
    }

    //JD-308
    @Test
    public void stcBugTest() throws Exception {
        scriptService.parseScriptStatic(FileUtil.readExample("stc-bug"), ImmutableMap.of());
    }

    //JD-318
    @Test
    @Disabled //todo: enable after JD-203
    public void stcGetAtObjectBugTest() throws Exception {
        scriptService.parseScriptStatic(FileUtil.readExample("tests/stc-getat-object-failure"), ImmutableMap.of());
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void jsonSlurperTest(boolean isStatic) throws Exception {
        String script = FileUtil.readExample("tests/jsonSlurper");

        Object result;
        if (isStatic) {
            result = scriptService.executeScriptStatic(null, script, ScriptType.ADMIN_SCRIPT, ImmutableMap.of(), ImmutableMap.of());
        } else {
            result = scriptService.executeScript(null, script, ScriptType.ADMIN_SCRIPT, ImmutableMap.of());
        }

        assertEquals(result, ImmutableMap.of("test", "value"));
    }

    @ParameterizedTest(name = "static: {0}")
    @MethodSource("createBooleanValues")
    public void withPluginParseTest(boolean isStatic) throws IOException {
        String script = FileUtil.readExample("tests/withPlugin");
        ParseContext parseContext;
        if (isStatic) {
            parseContext = scriptService.parseScript(script);
        } else {
            parseContext = scriptService.parseScriptStatic(script, ImmutableMap.of());
        }
        assertEquals(parseContext.getPlugins(), ImmutableSet.of("testPlugin"));
    }

    private ApplicationUser testUser() {
        return new MockApplicationUser("12323", "userName1337", "User Name 1337", "username@test.test");
    }
}
