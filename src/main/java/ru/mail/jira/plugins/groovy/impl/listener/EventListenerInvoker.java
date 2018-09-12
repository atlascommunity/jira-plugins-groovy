package ru.mail.jira.plugins.groovy.impl.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionDescriptor;
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionType;
import ru.mail.jira.plugins.groovy.api.dto.listener.ScriptedEventListener;
import ru.mail.jira.plugins.groovy.api.repository.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;

import java.util.Set;

@Component
public class EventListenerInvoker {
    private final Logger logger = LoggerFactory.getLogger(EventListenerInvoker.class);

    private final EventPublisher eventPublisher;

    private final EventListenerRepository eventListenerRepository;
    private final ScriptService scriptService;
    private final ExecutionRepository executionRepository;

    @Autowired
    public EventListenerInvoker(
        @ComponentImport EventPublisher eventPublisher,
        EventListenerRepository eventListenerRepository,
        ScriptService scriptService,
        ExecutionRepository executionRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.eventListenerRepository = eventListenerRepository;
        this.scriptService = scriptService;
        this.executionRepository = executionRepository;
    }

    public void onStart() {
        eventPublisher.register(this);
    }

    public void onStop() {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("Event {}", event.getClass());
        }

        if (event == null) {
            logger.warn("event is null");
            return;
        }

        for (ScriptedEventListener listener : eventListenerRepository.getAllListeners()) {
            ConditionDescriptor condition = listener.getCondition();
            if (condition.getType() == ConditionType.ISSUE) {
                Set<Long> typeIds = condition.getTypeIds();
                Set<Long> projectIds = condition.getProjectIds();
                Issue issue = event.getIssue();
                if (
                    (typeIds.isEmpty() || typeIds.contains(event.getEventTypeId())) &&
                    (projectIds.isEmpty() || projectIds.contains(issue.getProjectId()))
                ) {
                    executeScript(listener, event);
                }
            }
        }
    }

    @EventListener
    public void onEvent(Object event) {
        if (logger.isTraceEnabled()) {
            logger.trace("Event {}", event.getClass());
        }

        if (event == null) {
            logger.warn("event is null");
            return;
        }

        for (ScriptedEventListener listener : eventListenerRepository.getAllListeners()) {
            ConditionDescriptor condition = listener.getCondition();
            if (condition.getType() == ConditionType.CLASS_NAME) {
                if (condition.getClassInstance().isInstance(event)) {
                    executeScript(listener, event);
                }
            }
        }
    }

    private void executeScript(ScriptedEventListener listener, Object event) {
        String uuid = listener.getUuid();
        long t = System.currentTimeMillis();
        boolean successful = true;
        String error = null;

        try {
            scriptService.executeScript(
                uuid,
                listener.getScript(),
                ScriptType.LISTENER,
                ImmutableMap.of("event", event)
            );
        } catch (Exception e) {
            logger.error("Was unable to execute listener {}/{}", listener.getId(), uuid, e);
            successful = false;
            error = ExceptionHelper.writeExceptionToString(e);
        } finally {
            t = System.currentTimeMillis() - t;
        }

        if (!successful || t >= ExecutionRepository.WARNING_THRESHOLD) {
            executionRepository.trackInline(uuid, t, successful, error, ImmutableMap.of(
                "event", event.toString(),
                "type", ScriptType.LISTENER.name()
            ));
        }
    }
}
