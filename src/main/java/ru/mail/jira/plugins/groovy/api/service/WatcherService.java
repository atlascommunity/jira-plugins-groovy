package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;

import java.util.List;

public interface WatcherService {
    void addWatcher(EntityType type, Integer id, ApplicationUser user);

    void removeWatcher(EntityType type, Integer id, ApplicationUser user);

    List<ApplicationUser> getWatchers(EntityType type, Integer id);
}
