package ru.mail.jira.plugins.groovy.api.script;

import lombok.Getter;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.List;

public class ExecutionContext {
    @Getter
    private List<LoggingEvent> logEntries = new ArrayList<>();
}
