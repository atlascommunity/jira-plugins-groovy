package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerDto;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.impl.listener.ScriptedEventListener;

import java.util.List;

public interface EventListenerRepository {
    List<ScriptedEventListener> getAllListeners();

    List<EventListenerDto> getListeners(boolean includeChangelogs, boolean includeErrorCount);

    EventListenerDto getEventListener(int id);

    EventListenerDto createEventListener(ApplicationUser user, EventListenerForm form);

    EventListenerDto updateEventListener(ApplicationUser user, int id, EventListenerForm form);

    void deleteEventListener(ApplicationUser user, int id);

    void restoreEventListener(ApplicationUser user, int id);

    void invalidate();
}
