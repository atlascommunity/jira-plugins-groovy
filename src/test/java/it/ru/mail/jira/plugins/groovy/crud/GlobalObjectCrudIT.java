package it.ru.mail.jira.plugins.groovy.crud;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import it.ru.mail.jira.plugins.groovy.util.*;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectDto;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.entity.GlobalObjectChangelog;
import ru.mail.jira.plugins.groovy.api.repository.GlobalObjectRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.util.Const;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class GlobalObjectCrudIT {
    @Inject
    @ComponentImport
    private GlobalObjectRepository globalObjectRepository;
    @Inject
    @ComponentImport
    private ScriptService scriptService;

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

    private GlobalObjectForm createForm() {
        return Forms.globalObjectFromSource("class Test {}");
    }

    private GlobalObjectDto createScript(GlobalObjectForm form) {
        assertNotNull(form);

        return globalObjectRepository.create(userHelper.getAdmin(), form);
    }

    private GlobalObjectDto createScript() {
        return createScript(createForm());
    }

    private boolean isScriptExists(int id) {
        return globalObjectRepository.getAll().stream().anyMatch(it -> it.getId() == id);
    }

    private boolean isBindingExists(String bindingName) {
        return scriptService.getGlobalBindings().containsKey(bindingName);
    }

    @Test
    public void shouldCreateScript() {
        GlobalObjectForm form = createForm();
        GlobalObjectDto script = createScript(form);
        assertNotNull(script);
        assertTrue(form.matches(script));
        assertTrue(isScriptExists(script.getId()));
        assertTrue(isBindingExists(form.getName()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.GLOBAL_OBJECT, EntityAction.CREATED);
        changeLogHelper.assertChangeLogCreated(GlobalObjectChangelog.class, "SCRIPT_ID = ?", script.getId(), Const.CREATED_COMMENT, userHelper.getAdmin());
    }

    @Test
    public void shouldUpdateScriptSameName() {
        String comment = "comment" + System.currentTimeMillis();

        GlobalObjectDto script = createScript();

        GlobalObjectForm form = new GlobalObjectForm();
        form.setName(script.getName());
        form.setScriptBody("class Test {} //aaaa");
        form.setComment(comment);

        GlobalObjectDto updatedScript = globalObjectRepository.update(userHelper.getAdmin(), script.getId(), form);
        assertNotNull(updatedScript);
        assertTrue(form.matches(updatedScript));
        assertTrue(isBindingExists(form.getName()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.GLOBAL_OBJECT, EntityAction.UPDATED);
        changeLogHelper.assertChangeLogCreated(GlobalObjectChangelog.class, "SCRIPT_ID = ?", script.getId(), comment, userHelper.getAdmin());
    }

    @Test
    public void shouldUpdateScriptDifferentName() {
        String comment = "comment" + System.currentTimeMillis();

        GlobalObjectDto script = createScript();

        GlobalObjectForm form = new GlobalObjectForm();
        form.setName(script.getName() + "1111");
        form.setScriptBody("class Test {} //aaaa");
        form.setComment(comment);

        GlobalObjectDto updatedScript = globalObjectRepository.update(userHelper.getAdmin(), script.getId(), form);
        assertNotNull(updatedScript);
        assertTrue(form.matches(updatedScript));
        assertTrue(isBindingExists(updatedScript.getName()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.GLOBAL_OBJECT, EntityAction.UPDATED);
        changeLogHelper.assertChangeLogCreated(GlobalObjectChangelog.class, "SCRIPT_ID = ?", script.getId(), comment, userHelper.getAdmin());
    }

    @Test
    public void shouldDeleteScript() {
        GlobalObjectDto script = createScript();

        assertTrue(isScriptExists(script.getId()));
        globalObjectRepository.delete(userHelper.getAdmin(), script.getId());

        assertFalse(isScriptExists(script.getId()));
        assertFalse(isBindingExists(script.getName()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.GLOBAL_OBJECT, EntityAction.DELETED);
    }

    @Test
    public void shouldRestoreScript() {
        GlobalObjectDto script = createScript();

        assertTrue(isScriptExists(script.getId()));

        globalObjectRepository.delete(userHelper.getAdmin(), script.getId());
        assertFalse(isScriptExists(script.getId()));

        globalObjectRepository.restore(userHelper.getAdmin(), script.getId());
        assertTrue(isScriptExists(script.getId()));
        assertTrue(isBindingExists(script.getName()));

        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.GLOBAL_OBJECT, EntityAction.RESTORED);
    }
}
