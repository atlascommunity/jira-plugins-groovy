package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import ru.mail.jira.plugins.groovy.api.AuditLogRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Response getAuditLogPage(@QueryParam("offset") int offset) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();
            return auditLogRepository.getPagedEntries(offset, 50);
        }).getResponse();
    }
}
