package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableList;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.dto.admin.AdminScriptForm;
import ru.mail.jira.plugins.groovy.api.repository.AdminScriptRepository;
import ru.mail.jira.plugins.groovy.api.service.admin.AdminScriptService;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScriptManager;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Scanned
@Path("/adminScript")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@WebSudoRequired
public class AdminScriptResource {
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionHelper permissionHelper;
    private final AdminScriptRepository adminScriptRepository;
    private final BuiltInScriptManager builtInScriptManager;
    private final AdminScriptService adminScriptService;

    public AdminScriptResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        PermissionHelper permissionHelper,
        AdminScriptRepository adminScriptRepository,
        BuiltInScriptManager builtInScriptManager,
        AdminScriptService adminScriptService
    ) {
        this.authenticationContext = authenticationContext;
        this.permissionHelper = permissionHelper;
        this.adminScriptRepository = adminScriptRepository;
        this.builtInScriptManager = builtInScriptManager;
        this.adminScriptService = adminScriptService;
    }

    @Path("/all")
    @GET
    public Response getAllTasks() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return ImmutableList
                .builder()
                .addAll(builtInScriptManager.getAllScripts())
                .addAll(adminScriptRepository.getAllScripts())
                .build();
        }).getResponse();
    }

    @GET
    @Path("/{id}/changelogs")
    @WebSudoRequired
    public Response getChangelogs(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return adminScriptRepository.getChangelogs(id);
        }).getResponse();
    }

    @Path("/")
    @POST
    public Response createTask(AdminScriptForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return adminScriptRepository.createScript(authenticationContext.getLoggedInUser(), form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @Path("/{id}")
    @PUT
    public Response createTask(@PathParam("id") int id, AdminScriptForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return adminScriptRepository.updateScript(authenticationContext.getLoggedInUser(), id, form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @Path("/{id}")
    @DELETE
    public Response deleteTask(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            adminScriptRepository.deleteScript(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @Path("/{id}/restore")
    @POST
    public Response restoreTask(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            adminScriptRepository.restoreScript(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @Path("/{id}")
    @GET
    public Response getTask(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return adminScriptRepository.getScript(id, true, true);
        }).getResponse();
    }

    @Path("/run/builtIn/{key}")
    @POST
    public Response runBuiltInScript(@PathParam("key") String key, Map<String, String> rawParams) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return adminScriptService.runBuiltInScript(authenticationContext.getLoggedInUser(), key, rawParams);
        }).getResponse();
    }

    @Path("/run/user/{id}")
    @POST
    public Response runUserScript(@PathParam("id") int id, Map<String, String> rawParams) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return adminScriptService.runUserScript(authenticationContext.getLoggedInUser(), id, rawParams);
        }).getResponse();
    }
}
