package ru.mail.jira.plugins.groovy.util.cl;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ContextAwareClassLoader extends ClassLoader {
    private final Logger logger = LoggerFactory.getLogger(ContextAwareClassLoader.class);
    private final ThreadLocal<Deque<Set<Plugin>>> currentContext = ThreadLocal.withInitial(LinkedList::new);

    public void addPlugins(Collection<Plugin> plugins) {
        for (Plugin plugin : plugins) {
            if (plugin.getPluginState() != PluginState.ENABLED) {
                throw new RuntimeException("Plugin " + plugin.getKey() + " is not enabled");
            }
        }

        getContext().addAll(plugins);
    }

    public void startContext() {
        logger.trace("creating context");
        currentContext.get().push(new HashSet<>());
    }

    public void exitContext() {
        logger.trace("exiting context");

        Queue<Set<Plugin>> queue = getQueue();
        queue.remove();

        if (queue.isEmpty()) {
            logger.trace("clearing thread local state");
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
