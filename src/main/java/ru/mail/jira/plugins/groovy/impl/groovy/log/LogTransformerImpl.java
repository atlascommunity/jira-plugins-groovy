package ru.mail.jira.plugins.groovy.impl.groovy.log;

import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogTransformerImpl implements LogTransformer {
    private final Layout layout = new PatternLayout("%d %-5p [%t]: %m%n");

    @Override
    public String formatLog(List<LoggingEvent> events) {
        if (events == null || events.size() == 0) {
            return null;
        }

        return events
            .stream()
            .map(layout::format)
            .collect(Collectors.joining(""));
    }
}
