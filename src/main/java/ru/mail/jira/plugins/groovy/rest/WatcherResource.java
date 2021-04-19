package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.service.WatcherService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/watch")
public class WatcherResource {
    private final JiraAuthenticationContext authenticationContext;
    private final WatcherService watcherService;
    private final PermissionHelper permissionHelper;

    public WatcherResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        WatcherService watcherService,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.watcherService = watcherService;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/{type}/all")
    public Response getWatches(
        @PathParam("type") EntityType type
    ) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return watcherService.getWatches(type, authenticationContext.getLoggedInUser());
        }).getResponse();
    }

    @POST
    @Path("/{type}/{id}")
    public Response watch(
        @PathParam("type") EntityType type,
        @PathParam("id") int id
    ) {
        return new RestExecutor<Void>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            watcherService.addWatcher(type, id, authenticationContext.getLoggedInUser());

            return null;
        }).getResponse();
    }

    @DELETE
    @Path("/{type}/{id}")
    public Response unwatch(
        @PathParam("type") EntityType type,
        @PathParam("id") int id
    ) {
        return new RestExecutor<Void>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();
            watcherService.removeWatcher(type, id, authenticationContext.getLoggedInUser());

            return null;
        }).getResponse();
    }
}
