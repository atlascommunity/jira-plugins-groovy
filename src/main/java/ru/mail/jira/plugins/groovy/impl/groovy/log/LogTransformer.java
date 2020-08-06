package ru.mail.jira.plugins.groovy.impl.groovy.log;

import org.apache.log4j.spi.LoggingEvent;

import java.util.List;

public interface LogTransformer {
    String formatLog(List<LoggingEvent> events);
}
