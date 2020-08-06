package ru.mail.jira.plugins.groovy.impl.groovy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.script.ExecutionContext;

public class ExecutionContextHolder {
    private final Logger logger = LoggerFactory.getLogger(ExecutionContextHolder.class);
    private final ThreadLocal<ExecutionContext> context = ThreadLocal.withInitial(ExecutionContext::new);

    public ExecutionContext get() {
        if (logger.isTraceEnabled()) {
            logger.trace("accessing execution context");
        }
        return context.get();
    }

    public void reset() {
        context.remove();
    }
}
