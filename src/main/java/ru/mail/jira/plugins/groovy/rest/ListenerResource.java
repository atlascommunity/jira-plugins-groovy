package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.dto.EventListenerForm;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Scanned
@Path("/listener")
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
    public Response getAllListeners() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.getListeners();
        }).getResponse();
    }

    @POST
    @Path("/")
    public Response createListener(EventListenerForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.createEventListener(authenticationContext.getLoggedInUser(), form);
        }).getResponse();
    }

    @GET
    @Path("/{id}")
    public Response getListener(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.getEventListener(id);
        }).getResponse();
    }

    @PUT
    @Path("/{id}")
    public Response updateListener(@PathParam("id") int id, EventListenerForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return listenerRepository.updateEventListener(authenticationContext.getLoggedInUser(), id, form);
        }).getResponse();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteListener(@PathParam("id") int id) {
        return new RestExecutor<Void>(() -> {
            permissionHelper.checkIfAdmin();

            listenerRepository.deleteEventListener(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }
}
