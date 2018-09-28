package it.ru.mail.jira.plugins.groovy.crud;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.AuditLogHelper;
import it.ru.mail.jira.plugins.groovy.util.ChangeLogHelper;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionDescriptor;
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionType;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerDto;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.entity.ListenerChangelog;
import ru.mail.jira.plugins.groovy.api.repository.EventListenerRepository;
import ru.mail.jira.plugins.groovy.util.Const;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ListenerCrudIT {
    @Inject
    @ComponentImport
    private EventListenerRepository listenerRepository;

    @Inject
    private UserHelper userHelper;
    @Inject
    private AuditLogHelper auditLogHelper;
    @Inject
    protected ChangeLogHelper changeLogHelper;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive);
    }

    private EventListenerForm createScriptForm() {
        EventListenerForm form = new EventListenerForm();
        ConditionDescriptor condition = new ConditionDescriptor();
        condition.setType(ConditionType.ISSUE);

        form.setCondition(condition);
        form.setName("Script" + System.currentTimeMillis());
        form.setScriptBody("//");
        return form;
    }

    private EventListenerDto createScript(EventListenerForm form) {
        assertNotNull(form);

        return listenerRepository.createEventListener(userHelper.getAdmin(), form);
    }

    private EventListenerDto createScript() {
        return createScript(createScriptForm());
    }

    private boolean isScriptExists(int id) {
        return listenerRepository.getAllListeners().stream().anyMatch(it -> it.getId() == id);
    }

    @Test
    public void shouldCreateScript() {
        EventListenerForm form = createScriptForm();
        EventListenerDto script = createScript(form);
        assertNotNull(script);
        assertTrue(form.matches(script));
        assertTrue(isScriptExists(script.getId()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.LISTENER, EntityAction.CREATED);
        changeLogHelper.assertAuditLogCreated(ListenerChangelog.class, "LISTENER_ID = ?", script.getId(), Const.CREATED_COMMENT, userHelper.getAdmin());
    }

    @Test
    public void shouldUpdateScript() {
        String comment = "comment" + System.currentTimeMillis();

        EventListenerDto script = createScript();

        EventListenerForm form = new EventListenerForm();
        form.setName(script.getName());
        form.setScriptBody("//some code");
        form.setCondition(script.getCondition());
        form.setComment(comment);

        EventListenerDto updatedScript = listenerRepository.updateEventListener(userHelper.getAdmin(), script.getId(), form);
        assertNotNull(updatedScript);
        assertTrue(form.matches(updatedScript));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.LISTENER, EntityAction.UPDATED);
        changeLogHelper.assertAuditLogCreated(ListenerChangelog.class, "LISTENER_ID = ?", script.getId(), comment, userHelper.getAdmin());
    }

    @Test
    public void shouldDeleteScript() {
        EventListenerDto script = createScript();

        assertTrue(isScriptExists(script.getId()));
        listenerRepository.deleteEventListener(userHelper.getAdmin(), script.getId());

        assertFalse(isScriptExists(script.getId()));
        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.LISTENER, EntityAction.DELETED);
    }

    @Test
    public void shouldRestoreScript() {
        EventListenerDto script = createScript();

        assertTrue(isScriptExists(script.getId()));

        listenerRepository.deleteEventListener(userHelper.getAdmin(), script.getId());
        assertFalse(isScriptExists(script.getId()));

        listenerRepository.restoreEventListener(userHelper.getAdmin(), script.getId());
        assertTrue(isScriptExists(script.getId()));

        auditLogHelper.assertAuditLogCreated(script.getId(), EntityType.LISTENER, EntityAction.RESTORED);
    }
}
