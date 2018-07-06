package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.repository.AuditLogRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Scanned
@Path("/auditLog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditLogResource {
    private final PermissionHelper permissionHelper;
    private final AuditLogRepository auditLogRepository;

    public AuditLogResource(
        PermissionHelper permissionHelper,
        AuditLogRepository auditLogRepository
    ) {
        this.permissionHelper = permissionHelper;
        this.auditLogRepository = auditLogRepository;
    }

    @GET
    @Path("/all")
    @WebSudoRequired
    public Response getAuditLogPage(
        @QueryParam("offset") int offset,
        @QueryParam("user") Set<String> users,
        @QueryParam("category") Set<EntityType> categories,
        @QueryParam("action") Set<EntityAction> actions,
        @QueryParam("since") String since,
        @QueryParam("until") String until
    ) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();
            return auditLogRepository.getPagedEntries(offset, 50, users, categories, actions);
        }).getResponse();
    }
}
