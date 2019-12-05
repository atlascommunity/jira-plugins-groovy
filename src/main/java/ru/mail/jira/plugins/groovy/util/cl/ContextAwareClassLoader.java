package ru.mail.jira.plugins.groovy.util.cl;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ContextAwareClassLoader extends ClassLoader {
    private final ThreadLocal<Set<Plugin>> currentContext = ThreadLocal.withInitial(HashSet::new);

    public void addPlugins(Collection<Plugin> plugins) {
        for (Plugin plugin : plugins) {
            if (plugin.getPluginState() != PluginState.ENABLED) {
                throw new RuntimeException("Plugin " + plugin.getKey() + " is not enabled");
            }
        }

        currentContext.get().addAll(plugins);
    }

    public void clearContext() {
        currentContext.remove();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (Plugin plugin : currentContext.get()) {
            if (plugin.getPluginState() != PluginState.ENABLED) {
                throw new RuntimeException("Plugin " + plugin.getKey() + " is not enabled");
            }

            try {
                return plugin.getClassLoader().loadClass(name);
            } catch (ClassNotFoundException ignore) {
            }
        }

        throw new ClassNotFoundException(name);
    }
}
