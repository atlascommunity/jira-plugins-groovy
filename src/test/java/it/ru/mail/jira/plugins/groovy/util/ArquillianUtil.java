package it.ru.mail.jira.plugins.groovy.util;

import com.adaptavist.shrinkwrap.atlassian.plugin.api.AtlassianPluginArchive;
import com.google.common.collect.ImmutableSet;
import org.jboss.shrinkwrap.api.Archive;

import java.nio.file.Paths;
import java.util.Set;

public final class ArquillianUtil {
    private ArquillianUtil() {}

    public static AtlassianPluginArchive prepareArchive(Archive<?> archive) {
        return prepareArchive(archive, ImmutableSet.of());
    }

    public static AtlassianPluginArchive prepareArchive(Archive<?> archive, Set<String> requiredScripts) {
        AtlassianPluginArchive result = archive.as(AtlassianPluginArchive.class);
        result
            .addPackage("it.ru.mail.jira.plugins.groovy.util")
            .withSpringScannerOne(false);

        for (String script : requiredScripts) {
            result.addAsResource(Paths.get("src/examples/groovy/" + script + ".groovy").toFile(), script + ".groovy");
        }

        return result;
    }
}
