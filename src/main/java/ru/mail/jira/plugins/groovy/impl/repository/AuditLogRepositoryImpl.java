package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.AuditLogRepository;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryDto;
import ru.mail.jira.plugins.groovy.api.dto.audit.AuditLogEntryForm;
import ru.mail.jira.plugins.groovy.api.dto.Page;
import ru.mail.jira.plugins.groovy.api.entity.AuditLogEntry;
import ru.mail.jira.plugins.groovy.util.UserMapper;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final ActiveObjects activeObjects;
    private final DateTimeFormatter dateTimeFormatter;
    private final UserMapper userMapper;

    @Autowired
    public AuditLogRepositoryImpl(
        @ComponentImport ActiveObjects activeObjects,
        @ComponentImport DateTimeFormatter dateTimeFormatter,
        UserMapper userMapper
    ) {
        this.activeObjects = activeObjects;
        this.dateTimeFormatter = dateTimeFormatter;
        this.userMapper = userMapper;
    }

    @Override
    public void create(ApplicationUser user, AuditLogEntryForm form) {
        activeObjects.create(
            AuditLogEntry.class,
            new DBParam("DATE", new Timestamp(System.currentTimeMillis())),
            new DBParam("USER_KEY", user.getKey()),
            new DBParam("CATEGORY", form.getCategory()),
            new DBParam("ACTION", form.getAction()),
            new DBParam("DESCRIPTION", form.getDescription())
        );
    }

    @Override
    public Page<AuditLogEntryDto> getPagedEntries(int offset, int limit) {
        List<AuditLogEntryDto> results = Arrays
            .stream(
                activeObjects.find(
                    AuditLogEntry.class,
                    Query.select().order("ID DESC").limit(limit).offset(offset)
                )
            )
            .map(this::buildEntryDto)
            .collect(Collectors.toList());
        int totalCount = activeObjects.count(AuditLogEntry.class);

        return new Page<>(offset, limit, totalCount, results);
    }

    private AuditLogEntryDto buildEntryDto(AuditLogEntry auditLogEntry) {
        AuditLogEntryDto result = new AuditLogEntryDto();

        result.setDate(dateTimeFormatter.forLoggedInUser().format(auditLogEntry.getDate()));
        result.setId(auditLogEntry.getID());
        result.setUser(userMapper.buildUser(auditLogEntry.getUserKey()));
        result.setAction(auditLogEntry.getAction());
        result.setCategory(auditLogEntry.getCategory());
        result.setDescription(auditLogEntry.getDescription());

        return result;
    }
}
