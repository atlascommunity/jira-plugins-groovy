package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.dto.directory.ParentForm;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.dto.directory.ScriptDirectoryForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.RegistryScriptForm;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.workflow.search.AllScriptUsageCollector;
import ru.mail.jira.plugins.groovy.impl.workflow.search.ScriptUsageCollector;
import ru.mail.jira.plugins.groovy.impl.workflow.search.WorkflowSearchService;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/registry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistryResource {
    private final JiraAuthenticationContext authenticationContext;
    private final ScriptRepository scriptRepository;
    private final WorkflowSearchService workflowSearchService;
    private final PermissionHelper permissionHelper;

    public RegistryResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        ScriptRepository scriptRepository,
        WorkflowSearchService workflowSearchService,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.scriptRepository = scriptRepository;
        this.workflowSearchService = workflowSearchService;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/directory/all")
    @WebSudoRequired
    public Response getDirectories() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.getAllDirectories();
        }).getResponse();
    }

    @GET
    @Path("/script/all")
    @WebSudoRequired
    public Response getAllScripts() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.getAllScripts();
        }).getResponse();
    }

    @GET
    @Path("/directory/picker")
    @WebSudoRequired
    public Response getDirectoriesPicker() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.getAllDirectoriesForPicker();
        }).getResponse();
    }

    @GET
    @Path("/directory/{id}")
    @WebSudoRequired
    public Response getDirectory(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.getDirectory(id);
        }).getResponse();
    }

    @POST
    @Path("/directory")
    @WebSudoRequired
    public Response createDirectory(ScriptDirectoryForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.createDirectory(authenticationContext.getLoggedInUser(), form);
        }).getResponse();
    }

    @PUT
    @Path("/directory/{id}")
    @WebSudoRequired
    public Response updateDirectory(@PathParam("id") int id, ScriptDirectoryForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.updateDirectory(authenticationContext.getLoggedInUser(), id, form);
        }).getResponse();
    }

    @DELETE
    @Path("/directory/{id}")
    @WebSudoRequired
    public Response deleteDirectory(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            scriptRepository.deleteDirectory(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @POST
    @Path("/directory/{id}/restore")
    @WebSudoRequired
    public Response restoreDirectory(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            scriptRepository.restoreDirectory(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @PUT
    @Path("/directory/{id}/parent")
    @WebSudoRequired
    public Response moveDirectory(@PathParam("id") int id, ParentForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            scriptRepository.moveDirectory(authenticationContext.getLoggedInUser(), id, form);

            return null;
        }).getResponse();
    }

    @GET
    @Path("/script/{type}/all")
    @WebSudoRequired
    public Response getAllScripts(@PathParam("type") WorkflowScriptType workflowScriptType) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.getAllScriptDescriptions(workflowScriptType);
        }).getResponse();
    }

    @GET
    @Path("/script/{id}")
    @WebSudoRequired
    public Response getScript(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.getScript(id, true, false, false);
        }).getResponse();
    }

    @GET
    @Path("/script/{id}/changelogs")
    @WebSudoRequired
    public Response getScriptChangelogs(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return scriptRepository.getScriptChangelogs(id);
        }).getResponse();
    }

    @POST
    @Path("/script")
    @WebSudoRequired
    public Response createScript(RegistryScriptForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

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
            permissionHelper.checkIfAdminOrSysAdmin();
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
            permissionHelper.checkIfAdminOrSysAdmin();

            scriptRepository.deleteScript(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @POST
    @Path("/script/{id}/restore")
    @WebSudoRequired
    public Response restoreScript(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            scriptRepository.restoreScript(authenticationContext.getLoggedInUser(), id);

            return null;
        }).getResponse();
    }

    @PUT
    @Path("/script/{id}/parent")
    @WebSudoRequired
    public Response moveScript(@PathParam("id") int id, ParentForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            scriptRepository.moveScript(authenticationContext.getLoggedInUser(), id, form);

            return null;
        }).getResponse();
    }

    @GET
    @Path("/script/{id}/workflows")
    @WebSudoRequired
    public Response findScriptWorkflows(@PathParam("id") int id) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();
            return workflowSearchService.search(new ScriptUsageCollector(id)).getResult();
        }).getResponse();
    }

    @GET
    @Path("/workflowUsage")
    @WebSudoRequired
    public Response getWorkflowUsage() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            return workflowSearchService.search(new AllScriptUsageCollector()).getResult();
        }).getResponse();
    }
}
