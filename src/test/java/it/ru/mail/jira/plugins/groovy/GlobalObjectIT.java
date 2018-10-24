package it.ru.mail.jira.plugins.groovy;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectDto;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.api.repository.GlobalObjectRepository;
import ru.mail.jira.plugins.groovy.api.script.BindingDescriptor;
import ru.mail.jira.plugins.groovy.api.script.BindingProvider;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

//todo: make IT using same class name
@RunWith(Arquillian.class)
public class GlobalObjectIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/GlobalObject"
    );

    @Inject
    @ComponentImport
    private ScriptService scriptService;
    @Inject
    @ComponentImport
    private GlobalObjectRepository globalObjectRepository;
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

    @Before
    public void beforeEach() throws IOException {
        long ts = System.currentTimeMillis();
        globalObjectName = "testObject" + ts;

        GlobalObjectForm form = new GlobalObjectForm();
        form.setName(globalObjectName);
        form.setScriptBody(FileUtil.readArquillianExample("tests/GlobalObject").replaceAll("\\$TS\\$", String.valueOf(ts)));

        GlobalObjectDto globalObjectDto = globalObjectRepository.create(userHelper.getAdmin(), form);

        globalObjectId = globalObjectDto.getId();
    }

    @After
    public void afterEach() {
        if (globalObjectId != null) {
            globalObjectRepository.delete(userHelper.getAdmin(), globalObjectId);
        }
    }

    private String createScript() {
        return globalObjectName + ".getAdmin()";
    }

    private BindingDescriptor<?> findCurrentScriptBinding() {
        BindingDescriptor<?> binding = null;

        Map<String, BindingDescriptor<?>> bindings = bindingProvider.getBindings();
        if (bindings.containsKey(globalObjectName)) {
            binding = bindings.get(globalObjectName);
        }

        assertNotNull(binding);

        return binding;
    }

    @Test
    public void typeShouldMatch() {
        BindingDescriptor<?> binding = findCurrentScriptBinding();

        assertThat(binding.getValue(null), instanceOf(binding.getType()));
    }

    @Test
    public void shouldWork() throws Exception {
        Object result = scriptService.executeScript(null, createScript(), ScriptType.CONSOLE, ImmutableMap.of());

        assertEquals(userHelper.getAdmin(), result);
    }


    @Test
    public void staticShouldWork() throws Exception {
        Object result = scriptService.executeScriptStatic(null, createScript(), ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());

        assertEquals(userHelper.getAdmin(), result);
    }

    @Test
    public void checkType() throws Exception {
        BindingDescriptor<?> binding = findCurrentScriptBinding();

        Object result = scriptService.executeScriptStatic(null, globalObjectName + ".class", ScriptType.CONSOLE, ImmutableMap.of(), ImmutableMap.of());

        assertEquals(binding.getType(), result);
    }
}
