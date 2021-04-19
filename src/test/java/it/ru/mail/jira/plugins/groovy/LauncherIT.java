package it.ru.mail.jira.plugins.groovy;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.support.AopUtils;
import ru.mail.jira.plugins.groovy.api.PluginLauncher;
import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;

import javax.inject.Inject;
import java.util.List;

@RunWith(Arquillian.class)
public class LauncherIT {
    @ComponentImport
    @Inject
    private PluginLauncher pluginLauncher;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, ImmutableSet.of());
    }

    @Test
    public void pluginShouldBeInitialized() {
        Assert.assertTrue(pluginLauncher.isInitialized());
    }

    @Test
    public void initializationListShouldBeCorrect() {
        ImmutableList<String> classNames = ImmutableList.of(
            "ru.mail.jira.plugins.groovy.impl.sentry.SentryServiceImpl",
            "ru.mail.jira.plugins.groovy.impl.jsw.DelegatingJiraSoftwareHelper",
            "ru.mail.jira.plugins.groovy.impl.ScriptServiceImpl",
            "ru.mail.jira.plugins.groovy.impl.groovy.var.GlobalObjectsBindingProvider",
            "ru.mail.jira.plugins.groovy.impl.service.ScriptInvalidationServiceImpl",
            "ru.mail.jira.plugins.groovy.impl.jql.JqlInitializer",
            "ru.mail.jira.plugins.groovy.impl.jql.JqlFunctionServiceImpl",
            "ru.mail.jira.plugins.groovy.impl.jql.ModuleReplicationService",
            "ru.mail.jira.plugins.groovy.impl.listener.EventListenerInvoker",
            "ru.mail.jira.plugins.groovy.impl.scheduled.ScheduledTaskServiceImpl",
            "ru.mail.jira.plugins.groovy.impl.OldExecutionDeletionScheduler",
            "ru.mail.jira.plugins.groovy.impl.repository.ExecutionRepositoryImpl"
        );
        List<PluginLifecycleAware> actualObjects = pluginLauncher.getLifecycleAwareObjects();

        Assert.assertEquals(classNames.size(), actualObjects.size());

        int i = 0;
        for (String className : classNames) {
            Assert.assertEquals(className, AopUtils.getTargetClass(actualObjects.get(i++)).getName());
        }
    }
}
