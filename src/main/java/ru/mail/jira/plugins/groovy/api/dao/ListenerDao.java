package ru.mail.jira.plugins.groovy.api.dao;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.api.entity.Listener;

public interface ListenerDao {
    @Transactional
    Listener createEventListener(ApplicationUser user, EventListenerForm form);

    @Transactional
    Listener updateEventListener(ApplicationUser user, int id, EventListenerForm form);

    @Transactional
    void deleteEventListener(ApplicationUser user, int id);

    @Transactional
    void restoreEventListener(ApplicationUser user, int id);
}
