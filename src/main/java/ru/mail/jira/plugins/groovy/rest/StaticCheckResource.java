package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.google.common.collect.ImmutableList;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.StaticCheckForm;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.TypeUtil;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Scanned
@Path("/staticCheck")
public class StaticCheckResource {
    private final Logger logger = LoggerFactory.getLogger(StaticCheckResource.class);

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

            ParseContext parseContext = null;

            switch (form.getScriptType()) {
                case CONSOLE:
                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getConsoleTypes());
                    break;
                case WORKFLOW_GENERIC:
                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getWorkflowTypes());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported script type");
            }

            if (parseContext != null) {
                if (parseContext.getWarnings() == null) {
                    logger.error("warnings is null");
                } else {
                    return parseContext
                        .getWarnings()
                        .stream()
                        .map(msg -> ExceptionHelper.mapCompilationMessage("warning", msg))
                        .collect(Collectors.toList());
                }
            }

            return ImmutableList.of();
        })
            .withExceptionMapper(MultipleCompilationErrorsException.class, Response.Status.BAD_REQUEST, e -> ExceptionHelper.mapCompilationException("scriptBody", e))
            .getResponse();
    }
}
