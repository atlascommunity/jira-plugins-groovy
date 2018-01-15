package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditCategory;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerDto;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.api.entity.AuditAction;
import ru.mail.jira.plugins.groovy.api.entity.EventListener;
import ru.mail.jira.plugins.groovy.impl.listener.ScriptedEventListener;
import ru.mail.jira.plugins.groovy.impl.listener.condition.ConditionDescriptor;
import ru.mail.jira.plugins.groovy.impl.listener.condition.ConditionFactory;
import ru.mail.jira.plugins.groovy.util.JsonMapper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class EventListenerRepositoryImpl implements EventListenerRepository {
    private static final String VALUE_KEY = "value";

    private final Cache<String, List<ScriptedEventListener>> cache;
    private final ActiveObjects ao;
    private final ConditionFactory conditionFactory;
    private final JsonMapper jsonMapper;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public EventListenerRepositoryImpl(
        @ComponentImport CacheManager cacheManager,
        @ComponentImport ActiveObjects ao,
        ConditionFactory conditionFactory,
        JsonMapper jsonMapper,
        AuditLogRepository auditLogRepository
    ) {
        cache = cacheManager.getCache(EventListenerRepositoryImpl.class.getName() + ".cache",
            new EventListenerCacheLoader(),
            new CacheSettingsBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .remote()
                .replicateViaInvalidation()
                .build()
        );
        this.ao = ao;
        this.conditionFactory = conditionFactory;
        this.jsonMapper = jsonMapper;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<ScriptedEventListener> getAllListeners() {
        return cache.get(VALUE_KEY);
    }

    @Override
    public List<EventListenerDto> getListeners() {
        return Arrays
            .stream(ao.find(EventListener.class, Query.select().where("DELETED = ?", false)))
            .map(this::buildDto)
            .collect(Collectors.toList());
    }

    @Override
    public EventListenerDto getEventListener(int id) {
        return buildDto(ao.get(EventListener.class, id));
    }

    @Override
    public EventListenerDto createEventListener(ApplicationUser user, EventListenerForm form) {
        EventListener listener = ao.create(
            EventListener.class,
            new DBParam("UUID", UUID.randomUUID().toString()),
            new DBParam("NAME", form.getName()),
            new DBParam("SCRIPT", form.getScript()),
            new DBParam("AUTHOR_KEY", user.getKey()),
            new DBParam("DELETED", false),
            new DBParam("CONDITION", jsonMapper.write(form.getCondition()))
        );

        cache.remove(VALUE_KEY);

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.LISTENER,
                AuditAction.CREATED,
                listener.getID() + " - " + listener.getName()
            )
        );

        return buildDto(listener);
    }

    @Override
    public EventListenerDto updateEventListener(ApplicationUser user, int id, EventListenerForm form) {
        EventListener listener = ao.get(EventListener.class, id);

        if (listener == null || listener.isDeleted()) {
            throw new RuntimeException("Event listener is deleted");
        }

        listener.setName(form.getName());
        listener.setUuid(UUID.randomUUID().toString());
        listener.setScript(form.getScript());
        listener.setCondition(jsonMapper.write(form.getCondition()));
        listener.save();

        cache.remove(VALUE_KEY);

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.LISTENER,
                AuditAction.UPDATED,
                listener.getID() + " - " + listener.getName()
            )
        );

        return buildDto(listener);
    }

    @Override
    public void deleteEventListener(ApplicationUser user, int id) {
        EventListener listener = ao.get(EventListener.class, id);
        listener.setDeleted(true);
        listener.save();

        cache.remove(VALUE_KEY);

        auditLogRepository.create(
            user,
            new AuditLogEntryForm(
                AuditCategory.LISTENER,
                AuditAction.DELETED,
                listener.getID() + " - " + listener.getName()
            )
        );
    }

    @Override
    public void invalidate() {
        cache.removeAll();
    }

    private EventListenerDto buildDto(EventListener listener) {
        EventListenerDto result = new EventListenerDto();
        result.setId(listener.getID());
        result.setName(listener.getName());
        result.setScript(listener.getScript());
        result.setUuid(listener.getUuid());
        result.setCondition(jsonMapper.read(listener.getCondition(), ConditionDescriptor.class));
        return result;
    }

    private ScriptedEventListener buildEventListener(EventListener listener) {
        return new ScriptedEventListener(
            listener.getID(),
            listener.getScript(),
            listener.getUuid(),
            conditionFactory.create(jsonMapper.read(listener.getCondition(), ConditionDescriptor.class))
        );
    }

    private class EventListenerCacheLoader implements CacheLoader<String, List<ScriptedEventListener>> {
        @Nonnull
        @Override
        public List<ScriptedEventListener> load(@Nonnull String key) {
            if (Objects.equals(VALUE_KEY, key)) {
                return Arrays
                    .stream(ao.find(EventListener.class, Query.select().where("DELETED = ?", false)))
                    .map(EventListenerRepositoryImpl.this::buildEventListener)
                    .collect(Collectors.toList());
            } else {
                return ImmutableList.of();
            }
        }
    }
}
