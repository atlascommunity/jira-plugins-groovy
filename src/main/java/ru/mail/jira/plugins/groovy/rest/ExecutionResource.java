package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.dto.ScriptExecutionDto;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Scanned
@Path("/execution")
public class ExecutionResource {
    private final JiraAuthenticationContext authenticationContext;
    private final ExecutionRepository executionRepository;
    private final PermissionHelper permissionHelper;

    public ExecutionResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        ExecutionRepository executionRepository,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.executionRepository = executionRepository;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/forRegistry/{scriptId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ScriptExecutionDto> getExecutions(@PathParam("scriptId") int scriptId) {
        permissionHelper.checkIfAdmin(authenticationContext.getLoggedInUser());

        return executionRepository.getRegistryExecutions(scriptId);
    }

    @GET
    @Path("/forInline/{scriptId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ScriptExecutionDto> getExecutions(@PathParam("scriptId") String scriptId) {
        permissionHelper.checkIfAdmin(authenticationContext.getLoggedInUser());

        return executionRepository.getInlineExecutions(scriptId);
    }
}
