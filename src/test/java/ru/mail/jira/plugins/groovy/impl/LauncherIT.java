package ru.mail.jira.plugins.groovy.impl;

import com.adaptavist.shrinkwrap.atlassian.plugin.api.AtlassianPluginArchive;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
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
    public static Archive<?> useSpringScannerOne(Archive<?> archive) {
        return archive
            .as(AtlassianPluginArchive.class)
            .withSpringScannerOne(false);
    }

    @Test
    public void pluginShouldbeInitialized() {
        Assert.assertTrue(pluginLauncher.isInitialized());
    }
}
