package it.ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.entity.AbstractChangelog;
import ru.mail.jira.plugins.groovy.api.service.TestHelperService;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.*;

@Named
public class ChangeLogHelper {
    @Inject
    @ComponentImport
    private TestHelperService testHelperService;

    public void assertAuditLogCreated(Class<? extends AbstractChangelog> changelogClass, int id, String comment, ApplicationUser author) {
        AbstractChangelog[] changelogs = testHelperService.getActiveObjects().find(changelogClass, "SCRIPT_ID = ?", id);

        assertTrue(changelogs.length > 0);

        AbstractChangelog lastItem = changelogs[changelogs.length - 1];

        assertNotNull(lastItem);
        assertEquals(comment, lastItem.getComment());
        assertEquals(author.getKey(), lastItem.getAuthorKey());
    }
}
