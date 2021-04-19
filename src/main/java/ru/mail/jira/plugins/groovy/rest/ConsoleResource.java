package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.script.ScriptExecutionOutcome;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.console.ConsoleResponse;
import ru.mail.jira.plugins.groovy.api.dto.console.ScriptRequest;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.groovy.log.LogTransformer;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/scripts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsoleResource {
    private final JiraAuthenticationContext authenticationContext;
    private final ScriptService scriptService;
    private final PermissionHelper permissionHelper;
    private final LogTransformer logTransformer;

    public ConsoleResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        ScriptService scriptService,
        PermissionHelper permissionHelper,
        LogTransformer logTransformer
    ) {
        this.authenticationContext = authenticationContext;
        this.scriptService = scriptService;
        this.permissionHelper = permissionHelper;
        this.logTransformer = logTransformer;
    }

    @Path("/execute")
    @POST
    @WebSudoRequired
    public Response execute(ScriptRequest request) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            ApplicationUser currentUser = authenticationContext.getLoggedInUser();
            ScriptExecutionOutcome outcome = scriptService.executeScriptWithOutcome(null, request.getScript(), ScriptType.CONSOLE, ImmutableMap.of("currentUser", currentUser));
            return new ConsoleResponse(
                String.valueOf(outcome.getResult()),
                logTransformer.formatLog(outcome.getExecutionContext().getLogEntries()),
                outcome.getTime()
            );
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("script", e))
            .getResponse();
    }
}
