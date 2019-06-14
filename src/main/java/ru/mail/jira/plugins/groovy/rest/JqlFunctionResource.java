package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionForm;
import ru.mail.jira.plugins.groovy.api.repository.JqlFunctionRepository;
import ru.mail.jira.plugins.groovy.api.service.JqlFunctionService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/jql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@WebSudoRequired
public class JqlFunctionResource {
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionHelper permissionHelper;
    private final JqlFunctionRepository functionRepository;
    private final JqlFunctionService functionService;

    public JqlFunctionResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        PermissionHelper permissionHelper,
        JqlFunctionRepository functionRepository,
        JqlFunctionService functionService
    ) {
        this.authenticationContext = authenticationContext;
        this.permissionHelper = permissionHelper;
        this.functionRepository = functionRepository;
        this.functionService = functionService;
    }

    @Path("/all")
    @GET
    public Response getAllScripts() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return functionRepository.getAllScripts(false, true);
        }).getResponse();
    }

    @GET
    @Path("/{id}/changelogs")
    @WebSudoRequired
    public Response getChangelogs(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return functionRepository.getChangelogs(id);
        }).getResponse();
    }

    @Path("/")
    @POST
    public Response createScript(JqlFunctionForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return functionService.createScript(authenticationContext.getLoggedInUser(), form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @Path("/{id}")
    @PUT
    public Response createScript(@PathParam("id") int id, JqlFunctionForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return functionService.updateScript(authenticationContext.getLoggedInUser(), id, form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @Path("/{id}")
    @DELETE
    public Response deleteScript(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            functionService.deleteScript(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @Path("/{id}/restore")
    @POST
    public Response restoreScript(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            functionService.restoreScript(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @Path("/{id}")
    @GET
    public Response getScript(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return functionRepository.getScript(id);
        }).getResponse();
    }
}
