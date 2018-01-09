package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import ru.mail.jira.plugins.groovy.api.ExecutionRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Scanned
@Path("/execution")
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExecutions(@PathParam("scriptId") int scriptId) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return executionRepository.getRegistryExecutions(scriptId);
        }).getResponse();
    }

    @GET
    @Path("/forInline/{scriptId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExecutions(@PathParam("scriptId") String scriptId) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return executionRepository.getInlineExecutions(scriptId);
        }).getResponse();
    }
}
