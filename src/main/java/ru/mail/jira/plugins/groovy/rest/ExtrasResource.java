package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import ru.mail.jira.plugins.groovy.api.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.impl.cf.FieldValueExtractor;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Scanned
@Path("/extras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExtrasResource {
    private final PermissionHelper permissionHelper;
    private final ScriptInvalidationService scriptInvalidationService;
    private final EventListenerRepository listenerRepository;
    private final FieldConfigRepository fieldConfigRepository;

    public ExtrasResource(
        PermissionHelper permissionHelper,
        ScriptInvalidationService scriptInvalidationService,
        EventListenerRepository listenerRepository,
        FieldValueExtractor fieldValueExtractor,
        FieldConfigRepository fieldConfigRepository
    ) {
        this.permissionHelper = permissionHelper;
        this.scriptInvalidationService = scriptInvalidationService;
        this.listenerRepository = listenerRepository;
        this.fieldConfigRepository = fieldConfigRepository;
    }

    @POST
    @Path("/clearCache")
    @WebSudoRequired
    public Response invalidateCaches() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scriptInvalidationService.invalidateAllFields();
            scriptInvalidationService.invalidateAll();
            listenerRepository.invalidate();

            fieldConfigRepository.invalidateAll();

            return null;
        }).getResponse();
    }
}
