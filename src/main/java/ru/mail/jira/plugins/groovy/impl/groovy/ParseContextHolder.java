package ru.mail.jira.plugins.groovy.impl.groovy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseContextHolder {
    private final Logger logger = LoggerFactory.getLogger(ParseContextHolder.class);
    private final ThreadLocal<ParseContext> context = ThreadLocal.withInitial(ParseContext::new);

    public ParseContext get() {
        logger.info("accessing parse context");
        return context.get();
    }

    public void reset() {
        context.remove();
    }
}
