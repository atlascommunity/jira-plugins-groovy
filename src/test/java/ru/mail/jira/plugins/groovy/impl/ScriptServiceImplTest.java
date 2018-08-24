package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.GlobalFunctionManager;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;
import ru.mail.jira.plugins.groovy.util.DelegatingClassLoader;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScriptServiceImplTest {
    private ScriptService scriptService;

    @BeforeAll
    public void setup() {
        PluginEventManager pluginEventManager = new DefaultPluginEventManager();
        GlobalFunctionManager globalFunctionManager = new GlobalFunctionManagerImpl();
        DelegatingClassLoader delegatingClassLoader = new DelegatingClassLoader();

        scriptService = new ScriptServiceImpl(
            null,
            pluginEventManager,
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

        Map<String, Object> bindings = ImmutableMap.of(
            "user", new MockApplicationUser("userName1337")
        );

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
    public void scriptParamsTest(boolean isStatic) throws Exception {
        String script = FileUtil.readExample("admin-param");

        Map<String, Object> bindings = ImmutableMap.of(
            "user", new MockApplicationUser("12323", "userName1337", "User Name 1337", "username@test.test")
        );

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

    @Test
    public void stcBugTest() throws Exception {
        scriptService.parseScriptStatic(FileUtil.readExample("stc-bug"), ImmutableMap.of());
    }
}
