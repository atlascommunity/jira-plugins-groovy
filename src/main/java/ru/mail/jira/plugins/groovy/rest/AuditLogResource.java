package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import ru.mail.jira.plugins.groovy.api.AuditLogRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/auditLog")
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditLogPage(@QueryParam("offset") int offset) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();
            return auditLogRepository.getPagedEntries(offset, 50);
        }).getResponse();
    }
}
