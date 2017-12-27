package ru.mail.jira.plugins.groovy.impl.listener.condition;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ConditionFactory {
    private final EventTypeManager eventTypeManager;

    @Autowired
    public ConditionFactory(
        @ComponentImport EventTypeManager eventTypeManager
    ) {
        this.eventTypeManager = eventTypeManager;
    }

    public EventCondition create(ConditionDescriptor descriptor) {
        switch (descriptor.getType()) {
            case CLASS_NAME:
                return new ClassNameEventCondition(descriptor.getClassName());
            case ISSUE_PROJECT:
                return new ProjectCondition(descriptor.getEntityIds());
            case ISSUE_EVENT_TYPE:
                Set<Long> eventTypeIds = descriptor.getEntityIds();
                for (Long eventTypeId : eventTypeIds) {
                    EventType eventType = eventTypeManager.getEventType(eventTypeId);
                    if (eventType == null) {
                        throw new IllegalArgumentException("eventTypeId: Invalid value " + eventTypeId);
                    }
                }

                return new IssueEventTypeCondition(eventTypeIds);
            case OR:
                if (descriptor.getChildren() == null) {
                    throw new IllegalArgumentException("Children must be present");
                }

                return new OrCondition(
                    descriptor
                        .getChildren()
                        .stream()
                        .map(this::create)
                        .collect(Collectors.toList())
                );
            case AND:
                if (descriptor.getChildren() == null) {
                    throw new IllegalArgumentException("Children must be present");
                }

                return new AndCondition(
                    descriptor
                        .getChildren()
                        .stream()
                        .map(this::create)
                        .collect(Collectors.toList())
                );
        }
        throw new IllegalArgumentException("Unknown condition type");
    }
}
