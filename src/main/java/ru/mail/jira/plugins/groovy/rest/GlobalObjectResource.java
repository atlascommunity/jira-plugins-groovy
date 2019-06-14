package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.api.repository.GlobalObjectRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("go")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GlobalObjectResource {
    private final JiraAuthenticationContext authenticationContext;
    private final GlobalObjectRepository globalObjectRepository;
    private final PermissionHelper permissionHelper;

    public GlobalObjectResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        GlobalObjectRepository globalObjectRepository,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.globalObjectRepository = globalObjectRepository;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/all")
    @WebSudoRequired
    public Response getAll() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return globalObjectRepository.getAll();
        }).getResponse();
    }

    @GET
    @Path("/{id}/changelogs")
    @WebSudoRequired
    public Response getChangelogs(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return globalObjectRepository.getChangelogs(id);
        }).getResponse();
    }

    @GET
    @Path("/{id}")
    @WebSudoRequired
    public Response get(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return globalObjectRepository.get(id);
        }).getResponse();
    }

    @POST
    @Path("/")
    @WebSudoRequired
    public Response createListener(GlobalObjectForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return globalObjectRepository.create(authenticationContext.getLoggedInUser(), form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @PUT
    @Path("/{id}")
    @WebSudoRequired
    public Response updateListener(@PathParam("id") int id, GlobalObjectForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return globalObjectRepository.update(authenticationContext.getLoggedInUser(), id, form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @DELETE
    @Path("/{id}")
    @WebSudoRequired
    public Response deleteListener(@PathParam("id") int id) {
        return new RestExecutor<Void>(() -> {
            permissionHelper.checkIfAdmin();

            globalObjectRepository.delete(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @POST
    @Path("/{id}/restore")
    @WebSudoRequired
    public Response restoreListener(@PathParam("id") int id) {
        return new RestExecutor<Void>(() -> {
            permissionHelper.checkIfAdmin();

            globalObjectRepository.restore(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }
}
