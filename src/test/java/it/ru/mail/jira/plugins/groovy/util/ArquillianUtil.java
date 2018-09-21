package it.ru.mail.jira.plugins.groovy.util;

import com.adaptavist.shrinkwrap.atlassian.plugin.api.AtlassianPluginArchive;
import org.jboss.shrinkwrap.api.Archive;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import java.nio.file.Paths;
import java.util.Set;

public final class ArquillianUtil {
    private ArquillianUtil() {}

    public static AtlassianPluginArchive prepareArchive(Archive<?> archive, Set<String> requiredScripts) {
        AtlassianPluginArchive result = archive.as(AtlassianPluginArchive.class);
        result
            .addClass(FileUtil.class)
            .addPackage("it.ru.mail.jira.plugins.groovy.util")
            .withSpringScannerOne(false);

        for (String script : requiredScripts) {
            result.addAsResource(Paths.get("src/examples/groovy/" + script + ".groovy").toFile(), script + ".groovy");
        }

        return result;
    }
}
