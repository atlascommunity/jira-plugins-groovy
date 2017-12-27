package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.dto.EventListenerDto;
import ru.mail.jira.plugins.groovy.api.dto.EventListenerForm;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.ws.rs.*;
import java.util.List;

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
    public List<EventListenerDto> getAllListeners() {
        permissionHelper.checkIfAdmin(authenticationContext.getLoggedInUser());

        return listenerRepository.getListeners();
    }

    @POST
    @Path("/")
    public EventListenerDto createListener(EventListenerForm form) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        permissionHelper.checkIfAdmin(user);

        return listenerRepository.createEventListener(user, form);
    }

    @GET
    @Path("/{id}")
    public EventListenerDto getListener(@PathParam("id") int id) {
        permissionHelper.checkIfAdmin(authenticationContext.getLoggedInUser());
        return listenerRepository.getEventListener(id);
    }

    @PUT
    @Path("/{id}")
    public EventListenerDto updateListener(@PathParam("id") int id, EventListenerForm form) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        permissionHelper.checkIfAdmin(user);

        return listenerRepository.updateEventListener(user, id, form);
    }

    @DELETE
    @Path("/{id}")
    public void deleteListener(@PathParam("id") int id) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        permissionHelper.checkIfAdmin(user);

        listenerRepository.deleteEventListener(user, id);
    }
}
