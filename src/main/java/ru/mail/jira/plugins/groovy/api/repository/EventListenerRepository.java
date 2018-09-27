package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerDto;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.api.dto.listener.ScriptedEventListener;

import java.util.List;

public interface EventListenerRepository {
    List<ScriptedEventListener> getAllListeners();

    List<EventListenerDto> getListeners(boolean includeChangelogs, boolean includeErrorCount);

    EventListenerDto getEventListener(int id);

    @Transactional
    EventListenerDto createEventListener(ApplicationUser user, EventListenerForm form);

    @Transactional
    EventListenerDto updateEventListener(ApplicationUser user, int id, EventListenerForm form);

    @Transactional
    void deleteEventListener(ApplicationUser user, int id);

    @Transactional
    void restoreEventListener(ApplicationUser user, int id);

    void invalidate();
}
