package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.StaticCheckForm;
import ru.mail.jira.plugins.groovy.api.dto.error.PositionedCompilationMessage;
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlFunction;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.api.script.ParseContext;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.TypeUtil;
import ru.mail.jira.plugins.groovy.util.cl.DelegatingClassLoader;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.stream.Collectors;

@Scanned
@Path("/staticCheck")
public class StaticCheckResource {
    private final Logger logger = LoggerFactory.getLogger(StaticCheckResource.class);

    private final PermissionHelper permissionHelper;
    private final ScriptService scriptService;
    private final DelegatingClassLoader classLoader;

    public StaticCheckResource(
        PermissionHelper permissionHelper,
        ScriptService scriptService,
        DelegatingClassLoader classLoader
    ) {
        this.permissionHelper = permissionHelper;
        this.scriptService = scriptService;
        this.classLoader = classLoader;
    }

    @POST
    public Response checkStatic(StaticCheckForm form) {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdminOrSysAdmin();

            Map<String, String> additionalParams = form.getAdditionalParams();

            ParseContext parseContext = null;

            switch (form.getScriptType()) {
                case ADMIN_SCRIPT:
                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getAdminTypes());
                    break;
                case CONSOLE:
                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getConsoleTypes());
                    break;
                case WORKFLOW_GENERIC:
                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getWorkflowTypes());
                    break;
                case REST:
                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getRestTypes());
                    break;
                case CUSTOM_FIELD:
                    boolean velocityParamsEnabled = false;

                    if (additionalParams != null && additionalParams.containsKey("velocityParamsEnabled")) {
                        velocityParamsEnabled = "true".equals(additionalParams.get("velocityParamsEnabled"));
                    }

                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getFieldTypes(velocityParamsEnabled));
                    break;
                case SCHEDULED_TASK:
                    boolean isMutableIssue = false;
                    boolean withIssue = false;

                    if (additionalParams != null)
                        if (additionalParams.containsKey("withIssue")) {
                            withIssue = "true".equals(additionalParams.get("withIssue"));
                        }
                        if (additionalParams.containsKey("isMutableIssue")) {
                            isMutableIssue = "true".equals(additionalParams.get("isMutableIssue"));
                        }

                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getScheduledTypes(withIssue, isMutableIssue));
                    break;
                case JQL:
                    Class<?> functionClass = scriptService.parseSingleton(form.getScriptBody(), false, ImmutableMap.of()).getScriptClass();
                    InvokerHelper.removeClass(functionClass);
                    if (!ScriptedJqlFunction.class.isAssignableFrom(functionClass)) {
                        return Response
                            .status(400)
                            .entity(ImmutableMap.of(
                                "error", ImmutableList.of(
                                    ImmutableMap.of(
                                        "message", "Must implement ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlFunction"
                                    )
                                )
                            ))
                            .build();
                    }
                    break;
                case GLOBAL_OBJECT:
                    Class<?> objectClass = scriptService.parseSingleton(form.getScriptBody(), false, ImmutableMap.of()).getScriptClass();
                    //todo: check injections
                    InvokerHelper.removeClass(objectClass);
                    break;
                case LISTENER:
                    parseContext = scriptService.parseScriptStatic(form.getScriptBody(), TypeUtil.getListenerTypes());
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
