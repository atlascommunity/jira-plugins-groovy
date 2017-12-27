package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.dto.*;
import ru.mail.jira.plugins.groovy.api.dto.error.ScriptError;
import ru.mail.jira.plugins.groovy.api.dto.error.SyntaxError;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

//todo: rest executor
@Scanned
@Path("/repository")
public class RepositoryResource {
    private final JiraAuthenticationContext authenticationContext;
    private final ScriptRepository scriptRepository;
    private final PermissionHelper permissionHelper;

    public RepositoryResource(
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
    public List<ScriptDirectoryTreeDto> getDirectories() {
        checkPermissions();

        return scriptRepository.getAllDirectories();
    }

    @GET
    @Path("/directory/{id}")
    @WebSudoRequired
    public ScriptDirectoryDto getDirectory(@PathParam("id") int id) {
        checkPermissions();

        return scriptRepository.getDirectory(id);
    }

    @POST
    @Path("/directory")
    @WebSudoRequired
    public ScriptDirectoryDto createDirectory(ScriptDirectoryForm form) {
        checkPermissions();

        return scriptRepository.createDirectory(authenticationContext.getLoggedInUser(), form);
    }

    @PUT
    @Path("/directory/{id}")
    @WebSudoRequired
    public ScriptDirectoryDto updateDirectory(@PathParam("id") int id, ScriptDirectoryForm form) {
        checkPermissions();

        return scriptRepository.updateDirectory(authenticationContext.getLoggedInUser(), id, form);
    }

    @DELETE
    @Path("/directory/{id}")
    @WebSudoRequired
    public void deleteDirectory(@PathParam("id") int id) {
        checkPermissions();

        scriptRepository.deleteDirectory(authenticationContext.getLoggedInUser(), id);
    }

    @GET
    @Path("/script/{id}")
    @WebSudoRequired
    public ScriptDto getScript(@PathParam("id") int id) {
        checkPermissions();

        return scriptRepository.getScript(id);
    }

    @POST
    @Path("/script")
    @WebSudoRequired
    public Response createScript(ScriptForm form) {
        checkPermissions();

        //todo
        try {
            return Response.ok(scriptRepository.createScript(authenticationContext.getLoggedInUser(), form)).build();
        } catch (MultipleCompilationErrorsException e) {
            return Response.status(400).entity(e.getErrorCollector().getErrors()).build();
        }
    }

    @PUT
    @Path("/script/{id}")
    @WebSudoRequired
    public Response updateScript(@PathParam("id") int id, ScriptForm form) {
        checkPermissions();

        try {
            return Response.ok(scriptRepository.updateScript(authenticationContext.getLoggedInUser(), id, form)).build();
        } catch (MultipleCompilationErrorsException e) {
            return Response
                .status(400)
                .entity(
                    ImmutableMap.of(
                        "field", "scriptBody",
                        "error", e
                            .getErrorCollector()
                            .getErrors()
                            .stream()
                            .map(RepositoryResource::mapMessage)
                            .collect(Collectors.toList())
                    )
                )
                .build();
        }
    }

    @DELETE
    @Path("/script/{id}")
    @WebSudoRequired
    public void deleteScript(@PathParam("id") int id) {
        checkPermissions();

        scriptRepository.deleteScript(authenticationContext.getLoggedInUser(), id);
    }

    private static Object mapMessage(Object message) {
        if (message instanceof SyntaxErrorMessage) {
            return SyntaxError.fromErrorMessage((SyntaxErrorMessage) message);
        }
        if (message instanceof ExceptionMessage) {
            return new ScriptError(((ExceptionMessage) message).getCause().getMessage());
        }
        return null;
    }

    private void checkPermissions() {
        permissionHelper.checkIfAdmin(authenticationContext.getLoggedInUser());
    }
}
