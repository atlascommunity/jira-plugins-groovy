package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.entity.AbstractChangelog;
import ru.mail.jira.plugins.groovy.api.entity.FieldConfigChangelog;
import ru.mail.jira.plugins.groovy.api.service.TestHelperService;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.*;

@Named
public class ChangeLogHelper {
    @Inject
    @ComponentImport
    private TestHelperService testHelperService;

    private <T extends AbstractChangelog> T findLastItem(Class<T> changelogClass, String scriptQuery, int id) {
        T[] changelogs = testHelperService.getActiveObjects().find(changelogClass, scriptQuery, id);

        assertTrue(changelogs.length > 0);

        return changelogs[changelogs.length - 1];
    }

    public void assertChangeLogCreated(Class<? extends AbstractChangelog> changelogClass, int id, String comment, ApplicationUser author) {
        assertChangeLogCreated(changelogClass, "SCRIPT_ID = ?", id, comment, author);
    }

    public void assertChangeLogCreated(Class<? extends AbstractChangelog> changelogClass, String scriptQuery, int id, String comment, ApplicationUser author) {
        AbstractChangelog lastItem = findLastItem(changelogClass, scriptQuery, id);

        assertNotNull(lastItem);
        assertEquals(comment, lastItem.getComment());
        assertEquals(author.getKey(), lastItem.getAuthorKey());
    }

    public void assertChangeLogCreatedWithTemplate(int id) {
        FieldConfigChangelog lastItem = findLastItem(FieldConfigChangelog.class, "FIELD_CONFIG_ID = ?", id);

        assertNotNull(lastItem);
        assertNotNull(lastItem.getTemplateDiff());
    }
}
