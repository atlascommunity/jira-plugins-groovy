package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableMap;
import ru.mail.jira.plugins.groovy.api.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.ConsoleResponse;
import ru.mail.jira.plugins.groovy.api.dto.ScriptRequest;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Scanned
@Path("/scripts")
public class ConsoleResource {
    private final JiraAuthenticationContext authenticationContext;
    private final ScriptService scriptService;
    private final PermissionHelper permissionHelper;

    public ConsoleResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        ScriptService scriptService,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.scriptService = scriptService;
        this.permissionHelper = permissionHelper;
    }

    @Path("/execute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @WebSudoRequired
    public ConsoleResponse execute(ScriptRequest request) throws Exception {
        permissionHelper.checkIfAdmin(authenticationContext.getLoggedInUser());

        long startTime = System.currentTimeMillis();
        return new ConsoleResponse(
            String.valueOf(scriptService.executeScript(null, request.getScript(), ImmutableMap.of())),
            System.currentTimeMillis() - startTime
        );
    }
}
