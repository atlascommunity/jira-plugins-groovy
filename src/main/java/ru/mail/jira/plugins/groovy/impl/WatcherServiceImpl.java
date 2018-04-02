package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.entity.Watcher;
import ru.mail.jira.plugins.groovy.api.service.WatcherService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WatcherServiceImpl implements WatcherService {
    private final ActiveObjects ao;
    private final UserManager userManager;

    @Autowired
    public WatcherServiceImpl(
        @ComponentImport ActiveObjects ao,
        @ComponentImport UserManager userManager
    ) {
        this.ao = ao;
        this.userManager = userManager;
    }

    @Override
    public void addWatcher(EntityType type, Integer id, ApplicationUser user) {
        ao.create(
            Watcher.class,
            new DBParam("ENTITY_ID", id),
            new DBParam("TYPE", type),
            new DBParam("USER_KEY", user.getKey())
        );
    }

    @Override
    public void removeWatcher(EntityType type, Integer id, ApplicationUser user) {
        ao.deleteWithSQL(Watcher.class, "ENTITY_ID = ? AND TYPE = ? AND USER_KEY = ?", id, type, user.getKey());
    }

    @Override
    public List<ApplicationUser> getWatchers(EntityType type, Integer id) {
        return Arrays
            .stream(
                ao.find(
                    Watcher.class,
                    Query.select().where("ENTITY_ID = ? AND TYPE = ?", id, type)
                )
            )
            .map(Watcher::getUserKey)
            .map(this::getUser)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getWatches(EntityType type, ApplicationUser user) {
        return Arrays
            .stream(
                ao.find(
                    Watcher.class,
                    Query.select().where("TYPE = ? AND USER_KEY = ?", type, user.getKey())
                )
            )
            .map(Watcher::getEntityId)
            .collect(Collectors.toList());
    }

    private Optional<ApplicationUser> getUser(String key) {
        ApplicationUser user = userManager.getUserByKey(key);

        if (user != null && user.isActive()) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}
