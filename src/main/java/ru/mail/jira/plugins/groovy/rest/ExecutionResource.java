package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/execution")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExecutionResource {
    private final ExecutionRepository executionRepository;
    private final PermissionHelper permissionHelper;

    public ExecutionResource(
        ExecutionRepository executionRepository,
        PermissionHelper permissionHelper
    ) {
        this.executionRepository = executionRepository;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/forRegistry/{scriptId}")
    public Response getExecutions(@PathParam("scriptId") int scriptId) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return executionRepository.getRegistryExecutions(scriptId);
        }).getResponse();
    }

    @GET
    @Path("/forRegistry/{scriptId}/last")
    public Response getLastExecutions(@PathParam("scriptId") int scriptId) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return executionRepository.getLastRegistryExecutions(scriptId);
        }).getResponse();
    }

    @GET
    @Path("/forInline/{scriptId}")
    public Response getExecutions(@PathParam("scriptId") String scriptId) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return executionRepository.getInlineExecutions(scriptId);
        }).getResponse();
    }

    @GET
    @Path("/forInline/{scriptId}/last")
    public Response getLastExecutions(@PathParam("scriptId") String scriptId) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return executionRepository.getLastInlineExecutions(scriptId);
        }).getResponse();
    }
}
