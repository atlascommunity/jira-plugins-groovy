package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.repository.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/listener")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ListenerResource {
    private final JiraAuthenticationContext authenticationContext;
    private final EventListenerRepository listenerRepository;
    private final PermissionHelper permissionHelper;

    public ListenerResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        EventListenerRepository listenerRepository,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.listenerRepository = listenerRepository;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/all")
    @WebSudoRequired
    public Response getAllListeners() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.getListeners(true, true);
        }).getResponse();
    }

    @GET
    @Path("/{id}")
    @WebSudoRequired
    public Response getListener(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.getEventListener(id);
        }).getResponse();
    }

    @POST
    @Path("/")
    @WebSudoRequired
    public Response createListener(EventListenerForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.createEventListener(authenticationContext.getLoggedInUser(), form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @PUT
    @Path("/{id}")
    @WebSudoRequired
    public Response updateListener(@PathParam("id") int id, EventListenerForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.updateEventListener(authenticationContext.getLoggedInUser(), id, form);
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

            listenerRepository.deleteEventListener(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }
}
