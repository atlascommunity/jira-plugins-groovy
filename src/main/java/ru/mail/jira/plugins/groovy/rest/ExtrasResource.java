package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Scanned
@Path("/extras")
public class ExtrasResource {
    private final PermissionHelper permissionHelper;
    private final ScriptInvalidationService scriptInvalidationService;

    public ExtrasResource(
        PermissionHelper permissionHelper,
        ScriptInvalidationService scriptInvalidationService
    ) {
        this.permissionHelper = permissionHelper;
        this.scriptInvalidationService = scriptInvalidationService;
    }

    @POST
    @Path("/invalidateAll")
    public Response invalidateCaches() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scriptInvalidationService.invalidateAll();

            return null;
        }).getResponse();
    }
}
