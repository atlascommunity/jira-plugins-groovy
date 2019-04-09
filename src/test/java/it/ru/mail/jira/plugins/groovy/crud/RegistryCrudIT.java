package it.ru.mail.jira.plugins.groovy.crud;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.AuditLogHelper;
import it.ru.mail.jira.plugins.groovy.util.ChangeLogHelper;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.directory.*;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.entity.Changelog;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.util.Const;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class RegistryCrudIT {
    @Inject
    @ComponentImport
    private ScriptRepository scriptRepository;

    @Inject
    private UserHelper userHelper;
    @Inject
    private AuditLogHelper auditLogHelper;
    @Inject
    private ChangeLogHelper changeLogHelper;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive);
    }

    private ScriptDirectoryDto createDirectory() {
        ScriptDirectoryForm directoryForm = new ScriptDirectoryForm();
        directoryForm.setName("dir"+System.currentTimeMillis());

        return scriptRepository.createDirectory(userHelper.getAdmin(), directoryForm);
    }

    private RegistryScriptForm createScriptForm(ScriptDirectoryDto directory) {
        RegistryScriptForm form = new RegistryScriptForm();
        form.setName("Script" + System.currentTimeMillis());
        form.setDirectoryId(directory.getId());
        form.setScriptBody("//");
        form.setTypes(ImmutableSet.of(WorkflowScriptType.CONDITION));
        return form;
    }

    private RegistryScriptDto createScript(RegistryScriptForm form) {
        assertNotNull(form);

        return scriptRepository.createScript(userHelper.getAdmin(), form);
    }

    private RegistryScriptDto createScript(ScriptDirectoryDto directory) {
        assertNotNull(directory);

        return scriptRepository.createScript(userHelper.getAdmin(), createScriptForm(directory));
    }

    private RegistryScriptDto createScript() {
        return createScript(createDirectory());
    }

    private boolean isScriptExists(int id) {
        return scriptRepository.getAllScripts().stream().anyMatch(it -> it.getId().equals(id));
    }

    private boolean isDirectoryExists(int id) {
        return scriptRepository.getAllDirectories().stream().anyMatch(it -> it.getId().equals(id));
    }

    @Test
    public void shouldCreateDirectory() {
        ScriptDirectoryDto directory = createDirectory();

        assertNotNull(directory);
        assertTrue(isDirectoryExists(directory.getId()));
        auditLogHelper.assertAuditLogCreated(directory.getId(), EntityType.REGISTRY_DIRECTORY, EntityAction.CREATED);
    }

    @Test
    public void shouldDeleteDirectory() {
        ScriptDirectoryDto directory = createDirectory();

        assertTrue(isDirectoryExists(directory.getId()));
        scriptRepository.deleteDirectory(userHelper.getAdmin(), directory.getId());
        assertFalse(isDirectoryExists(directory.getId()));
        auditLogHelper.assertAuditLogCreated(directory.getId(), EntityType.REGISTRY_DIRECTORY, EntityAction.DELETED);
    }

    @Test
    public void shouldCreateScript() {
        ScriptDirectoryDto directory = createDirectory();

        RegistryScriptForm form = createScriptForm(directory);
        RegistryScriptDto script = createScript(form);
        assertNotNull(script);
        assertTrue(form.matches(script));
        assertTrue(isScriptExists(script.getId()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.REGISTRY_SCRIPT, EntityAction.CREATED);
        changeLogHelper.assertChangeLogCreated(Changelog.class, script.getId(), Const.CREATED_COMMENT, userHelper.getAdmin());
    }

    @Test
    public void shouldUpdateScript() {
        String comment = "comment" + System.currentTimeMillis();

        RegistryScriptDto script = createScript();

        RegistryScriptForm form = new RegistryScriptForm();
        form.setName(script.getName());
        form.setDirectoryId(script.getDirectoryId());
        form.setScriptBody("//some code");
        form.setTypes(script.getTypes());
        form.setComment(comment);

        RegistryScriptDto updatedScript = scriptRepository.updateScript(userHelper.getAdmin(), script.getId(), form);
        assertNotNull(updatedScript);
        assertTrue(form.matches(updatedScript));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.REGISTRY_SCRIPT, EntityAction.UPDATED);
        changeLogHelper.assertChangeLogCreated(Changelog.class, script.getId(), comment, userHelper.getAdmin());
    }

    @Test
    public void shouldDeleteScript() {
        RegistryScriptDto script = createScript();

        assertTrue(isScriptExists(script.getId()));
        scriptRepository.deleteScript(userHelper.getAdmin(), script.getId());

        assertFalse(isScriptExists(script.getId()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.REGISTRY_SCRIPT, EntityAction.DELETED);
    }

    @Test
    public void shouldRestoreScript() {
        RegistryScriptDto script = createScript();

        assertTrue(isScriptExists(script.getId()));

        scriptRepository.deleteScript(userHelper.getAdmin(), script.getId());
        assertFalse(isScriptExists(script.getId()));

        scriptRepository.restoreScript(userHelper.getAdmin(), script.getId());
        assertTrue(isScriptExists(script.getId()));

        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.REGISTRY_SCRIPT, EntityAction.RESTORED);
    }

    @Test
    public void shouldMoveScript() {
        RegistryScriptDto script = createScript();
        ScriptDirectoryDto newDir = createDirectory();

        ParentForm form = new ParentForm();
        form.setParentId(newDir.getId());
        scriptRepository.moveScript(userHelper.getAdmin(), script.getId(), form);

        RegistryScriptDto updatedScript = scriptRepository.getScript(script.getId(), false, false, false);

        assertEquals(newDir.getId(), updatedScript.getDirectoryId());
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.REGISTRY_SCRIPT, EntityAction.MOVED);
    }
}
