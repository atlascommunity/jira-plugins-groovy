package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableMap;
import ru.mail.jira.plugins.groovy.api.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.ConsoleResponse;
import ru.mail.jira.plugins.groovy.api.dto.ScriptRequest;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/scripts")
public class ConsoleResource {
    private final ScriptService scriptService;
    private final PermissionHelper permissionHelper;

    public ConsoleResource(
        ScriptService scriptService,
        PermissionHelper permissionHelper
    ) {
        this.scriptService = scriptService;
        this.permissionHelper = permissionHelper;
    }

    @Path("/execute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @WebSudoRequired
    public Response execute(ScriptRequest request) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            long startTime = System.currentTimeMillis();
            return new ConsoleResponse(
                String.valueOf(scriptService.executeScript(null, request.getScript(), ScriptType.CONSOLE, ImmutableMap.of())),
                System.currentTimeMillis() - startTime
            );
        }).getResponse();
    }
}
