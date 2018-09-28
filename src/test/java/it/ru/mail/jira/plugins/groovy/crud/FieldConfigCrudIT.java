package it.ru.mail.jira.plugins.groovy.crud;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import it.ru.mail.jira.plugins.groovy.util.*;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ofbiz.core.entity.GenericEntityException;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigDto;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.entity.FieldConfigChangelog;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.util.Const;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class FieldConfigCrudIT {
    @Inject
    @ComponentImport
    private FieldConfigRepository fieldConfigRepository;

    @Inject
    private UserHelper userHelper;
    @Inject
    private AuditLogHelper auditLogHelper;
    @Inject
    private ChangeLogHelper changeLogHelper;
    @Inject
    private CustomFieldHelper customFieldHelper;

    private CustomField field;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive);
    }

    @Before
    public void beforeEach() throws GenericEntityException {
        field = customFieldHelper.createNumberField();
    }

    @After
    public void afterEach() throws RemoveException {
        if (field != null) {
            customFieldHelper.deleteField(field);
        }
    }

    private FieldConfigForm createScriptForm() {
        FieldConfigForm form = new FieldConfigForm();
        form.setCacheable(true);
        form.setScriptBody("//");
        return form;
    }

    private FieldConfigDto createScript(FieldConfigForm form) {
        assertNotNull(form);

        return fieldConfigRepository.updateConfig(userHelper.getAdmin(), customFieldHelper.getFirstConfig(field).getId(), form);
    }

    private FieldConfigDto createScript() {
        return createScript(createScriptForm());
    }

    private boolean isScriptExists(long id) {
        return fieldConfigRepository.getAllConfigs().stream().anyMatch(it -> it.getId() == id);
    }

    @Test
    public void shouldCreateScript() {
        FieldConfigForm form = createScriptForm();
        FieldConfigDto script = createScript(form);
        assertNotNull(script);
        assertTrue(form.matches(script));
        assertTrue(isScriptExists(script.getId()));
        auditLogHelper.assertAuditLogCreated(script.getFieldScriptId(), script.getId(), EntityType.CUSTOM_FIELD, EntityAction.CREATED);
        changeLogHelper.assertChangeLogCreated(FieldConfigChangelog.class, "FIELD_CONFIG_ID = ?", script.getFieldScriptId(), Const.CREATED_COMMENT, userHelper.getAdmin());
    }

    @Test
    public void shouldUpdateScript() {
        String comment = "comment" + System.currentTimeMillis();

        FieldConfigDto script = createScript();

        FieldConfigForm form = new FieldConfigForm();
        form.setScriptBody("//some code");
        form.setComment(comment);
        form.setCacheable(script.isCacheable());
        form.setVelocityParamsEnabled(script.isVelocityParamsEnabled());
        form.setTemplate(script.getTemplate());

        FieldConfigDto updatedScript = fieldConfigRepository.updateConfig(userHelper.getAdmin(), script.getId(), form);
        assertNotNull(updatedScript);
        assertTrue(form.matches(updatedScript));
        auditLogHelper.assertAuditLogCreated(script.getFieldScriptId(), script.getId(), EntityType.CUSTOM_FIELD, EntityAction.UPDATED);
        changeLogHelper.assertChangeLogCreated(FieldConfigChangelog.class, "FIELD_CONFIG_ID = ?", script.getFieldScriptId(), comment, userHelper.getAdmin());
    }
}
