package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.dto.IssueEventType;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Scanned
@Path("/jira-api")
public class JiraApiResource {
    private final EventTypeManager eventTypeManager;
    private final PermissionHelper permissionHelper;

    public JiraApiResource(
        @ComponentImport EventTypeManager eventTypeManager,
        PermissionHelper permissionHelper
    ) {
        this.eventTypeManager = eventTypeManager;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/eventType")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IssueEventType> getIssueEventTypes() {
        permissionHelper.checkIfAdmin();

        return eventTypeManager
            .getEventTypes()
            .stream()
            .map(JiraApiResource::mapEventType)
            .collect(Collectors.toList());
    }

    private static IssueEventType mapEventType(EventType type) {
        IssueEventType issueEventType = new IssueEventType();
        issueEventType.setId(type.getId());
        issueEventType.setName(type.getName());
        return issueEventType;
    }
}
