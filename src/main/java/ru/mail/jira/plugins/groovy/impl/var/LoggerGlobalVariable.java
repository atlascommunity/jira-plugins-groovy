package ru.mail.jira.plugins.groovy.impl.var;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerGlobalVariable implements GlobalVariable<Logger> {
    private final Logger defaultLogger = LoggerFactory.getLogger("ru.mail.jira.plugins.groovy.script.$script$");

    @Override
    public Logger getValue(String scriptId) {
        if (scriptId != null) {
            return LoggerFactory.getLogger("ru.mail.jira.plugins.groovy.script." + scriptId);
        } else {
            return defaultLogger;
        }
    }

    @Override
    public Class<Logger> getType() {
        return Logger.class;
    }

    @Override
    public void dispose() {}
}
