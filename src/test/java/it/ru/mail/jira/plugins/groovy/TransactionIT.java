package it.ru.mail.jira.plugins.groovy;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.service.TestHelperService;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class TransactionIT {
    @Inject
    @ComponentImport
    private TestHelperService transactionService;

    @Inject
    @ComponentImport
    private ScriptRepository scriptRepository;

    @Inject
    private UserHelper userHelper;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive);
    }

    @Test
    public void transactionalOnInterfaceShouldRollback() {
        String directoryName = "testdir" + System.currentTimeMillis();

        try {
            transactionService.transactional(userHelper.getAdmin(), directoryName);
            fail("should've thrown exception");
        } catch (RuntimeException ignore) { }

        assertFalse(
            scriptRepository
                .getAllDirectories()
                .stream()
                .anyMatch(it -> directoryName.equals(it.getName()))
        );
    }

    @Test
    public void nonTransactionalShouldNotRollback() {
        String directoryName = "testdir" + System.currentTimeMillis();

        try {
            transactionService.nonTransactional(userHelper.getAdmin(), directoryName);
            fail("should've thrown exception");
        } catch (RuntimeException ignore) {}

        assertTrue(
            scriptRepository
                .getAllDirectories()
                .stream()
                .anyMatch(it -> directoryName.equals(it.getName()))
        );
    }
}
