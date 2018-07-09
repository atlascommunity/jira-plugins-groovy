package ru.mail.jira.plugins.groovy.impl.var;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerGlobalVariable implements GlobalVariable<Logger> {
    private final Logger logger = LoggerFactory.getLogger("ru.mail.jira.plugins.groovy.$script$");

    @Override
    public Logger getValue() {
        return this.logger;
    }

    @Override
    public Class<Logger> getType() {
        return Logger.class;
    }

    @Override
    public void dispose() {}
}
