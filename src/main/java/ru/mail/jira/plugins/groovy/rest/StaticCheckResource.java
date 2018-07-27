package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import ru.mail.jira.plugins.groovy.api.dto.StaticCheckForm;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.TypeUtil;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Scanned
@Path("/staticCheck")
public class StaticCheckResource {
    private final PermissionHelper permissionHelper;
    private final ScriptService scriptService;

    public StaticCheckResource(
        PermissionHelper permissionHelper,
        ScriptService scriptService
    ) {
        this.permissionHelper = permissionHelper;
        this.scriptService = scriptService;
    }

    @POST
    public Response checkStatic(StaticCheckForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            switch (form.getScriptType()) {
                case CONSOLE:
                    scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getConsoleTypes());
                    break;
                case WORKFLOW_GENERIC:
                    scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getWorkflowTypes());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported script type");
            }

            return null;
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }
}
