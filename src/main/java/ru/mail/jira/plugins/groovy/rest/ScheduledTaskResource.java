package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskForm;
import ru.mail.jira.plugins.groovy.api.repository.ScheduledTaskRepository;
import ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/scheduled")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@WebSudoRequired
public class ScheduledTaskResource {
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionHelper permissionHelper;
    private final ScheduledTaskService scheduledTaskService;
    private final ScheduledTaskRepository scheduledTaskRepository;

    public ScheduledTaskResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        PermissionHelper permissionHelper,
        ScheduledTaskService scheduledTaskService,
        ScheduledTaskRepository scheduledTaskRepository
    ) {
        this.authenticationContext = authenticationContext;
        this.permissionHelper = permissionHelper;
        this.scheduledTaskService = scheduledTaskService;
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    @Path("/all")
    @GET
    public Response getAllTasks() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scheduledTaskRepository.getAllTasks(true, true);
        }).getResponse();
    }

    @Path("/")
    @POST
    public Response createTask(ScheduledTaskForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scheduledTaskService.createTask(authenticationContext.getLoggedInUser(), form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @Path("/{id}")
    @PUT
    public Response createTask(@PathParam("id") int id, ScheduledTaskForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scheduledTaskService.updateTask(authenticationContext.getLoggedInUser(), id, form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @Path("/{id}")
    @DELETE
    public Response deleteTask(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scheduledTaskService.deleteTask(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @Path("/{id}/restore")
    @POST
    public Response restoreTask(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scheduledTaskService.restoreTask(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @Path("/{id}/enabled/{enabled}")
    @POST
    public Response setEnabled(@PathParam("id") int id, @PathParam("enabled") boolean enabled) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scheduledTaskService.setEnabled(authenticationContext.getLoggedInUser(), id, enabled);

            return null;
        }).getResponse();
    }

    @Path("/{id}")
    @GET
    public Response getTask(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scheduledTaskRepository.getTaskInfo(id, true, true);
        }).getResponse();
    }

    @Path("/{id}/runNow")
    @POST
    public Response runNow(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scheduledTaskService.runNow(authenticationContext.getLoggedInUser(), id);
        }).getResponse();
    }
}
