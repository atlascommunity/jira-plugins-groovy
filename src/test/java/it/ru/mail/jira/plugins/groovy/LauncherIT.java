package it.ru.mail.jira.plugins.groovy;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.PluginLauncher;

import javax.inject.Inject;

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
    public void pluginShouldbeInitialized() {
        Assert.assertTrue(pluginLauncher.isInitialized());
    }
}
