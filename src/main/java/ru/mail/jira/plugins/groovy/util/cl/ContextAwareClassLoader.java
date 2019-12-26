package ru.mail.jira.plugins.groovy.util.cl;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ContextAwareClassLoader extends ClassLoader {
    private final ThreadLocal<Queue<Set<Plugin>>> currentContext = ThreadLocal.withInitial(LinkedList::new);

    public void addPlugins(Collection<Plugin> plugins) {
        for (Plugin plugin : plugins) {
            if (plugin.getPluginState() != PluginState.ENABLED) {
                throw new RuntimeException("Plugin " + plugin.getKey() + " is not enabled");
            }
        }

        getContext().addAll(plugins);
    }

    public void startContext() {
        currentContext.get().add(new HashSet<>());
    }

    public void exitContext() {
        Queue<Set<Plugin>> queue = getQueue();
        queue.remove();

        if (queue.isEmpty()) {
            currentContext.remove();
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (Plugin plugin : getContext()) {
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

    private Queue<Set<Plugin>> getQueue() {
        Queue<Set<Plugin>> activeContext = currentContext.get();

        if (activeContext.isEmpty()) {
            throw new IllegalStateException("context is empty");
        }

        return activeContext;
    }

    private Set<Plugin> getContext() {
        return getQueue().peek();
    }
}
