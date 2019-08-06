package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.dto.cf.PreviewForm;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.cf.FieldPreviewService;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/fieldConfig")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@WebSudoRequired
public class FieldConfigResource {
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionHelper permissionHelper;
    private final FieldConfigRepository fieldConfigRepository;
    private final FieldPreviewService fieldPreviewService;

    public FieldConfigResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        PermissionHelper permissionHelper,
        FieldConfigRepository fieldConfigRepository,
        FieldPreviewService fieldPreviewService
    ) {
        this.authenticationContext = authenticationContext;
        this.permissionHelper = permissionHelper;
        this.fieldConfigRepository = fieldConfigRepository;
        this.fieldPreviewService = fieldPreviewService;
    }

    @Path("/all")
    @GET
    public Response getAllConfigs() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return fieldConfigRepository.getAllConfigs();
        }).getResponse();
    }

    @GET
    @Path("/{id}/changelogs")
    @WebSudoRequired
    public Response getChangelogs(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return fieldConfigRepository.getChangelogs(id);
        }).getResponse();
    }

    @Path("/{id}")
    @GET
    public Response getFieldConfig(@PathParam("id") long id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return fieldConfigRepository.getConfig(id, true);
        }).getResponse();
    }

    @Path("/{id}")
    @PUT
    public Response updateFieldConfig(@PathParam("id") long id, FieldConfigForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return fieldConfigRepository.updateConfig(authenticationContext.getLoggedInUser(), id, form);
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }

    @Path("/{id}/preview")
    @POST
    public Response previewFieldConfig(@PathParam("id") long id, PreviewForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return fieldPreviewService.preview(authenticationContext.getLoggedInUser(), id, form);
        }).getResponse();
    }
}
