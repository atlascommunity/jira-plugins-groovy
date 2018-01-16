package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.dto.directory.ScriptDirectoryForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.RegistryScriptForm;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Scanned
@Path("/registry")
public class RegistryResource {
    private final JiraAuthenticationContext authenticationContext;
    private final ScriptRepository scriptRepository;
    private final PermissionHelper permissionHelper;

    public RegistryResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        ScriptRepository scriptRepository,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.scriptRepository = scriptRepository;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/directory/all")
    @WebSudoRequired
    public Response getDirectories() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptRepository.getAllDirectories();
        }).getResponse();
    }

    @GET
    @Path("/directory/{id}")
    @WebSudoRequired
    public Response getDirectory(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptRepository.getDirectory(id);
        }).getResponse();
    }

    @POST
    @Path("/directory")
    @WebSudoRequired
    public Response createDirectory(ScriptDirectoryForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptRepository.createDirectory(authenticationContext.getLoggedInUser(), form);
        }).getResponse();
    }

    @PUT
    @Path("/directory/{id}")
    @WebSudoRequired
    public Response updateDirectory(@PathParam("id") int id, ScriptDirectoryForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptRepository.updateDirectory(authenticationContext.getLoggedInUser(), id, form);
        }).getResponse();
    }

    @DELETE
    @Path("/directory/{id}")
    @WebSudoRequired
    public Response deleteDirectory(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scriptRepository.deleteDirectory(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @GET
    @Path("/script/all")
    @WebSudoRequired
    public Response getAllScripts() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptRepository.getAllScriptDescriptions();
        }).getResponse();
    }

    @GET
    @Path("/script/{id}")
    @WebSudoRequired
    public Response getScript(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptRepository.getScript(id, true, false);
        }).getResponse();
    }

    @POST
    @Path("/script")
    @WebSudoRequired
    public Response createScript(RegistryScriptForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptRepository.createScript(authenticationContext.getLoggedInUser(), form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @PUT
    @Path("/script/{id}")
    @WebSudoRequired
    public Response updateScript(@PathParam("id") int id, RegistryScriptForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();
            return scriptRepository.updateScript(authenticationContext.getLoggedInUser(), id, form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @DELETE
    @Path("/script/{id}")
    @WebSudoRequired
    public Response deleteScript(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scriptRepository.deleteScript(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }
}
