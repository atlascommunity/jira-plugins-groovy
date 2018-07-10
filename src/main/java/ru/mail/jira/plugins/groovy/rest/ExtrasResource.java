package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import ru.mail.jira.plugins.groovy.api.repository.EventListenerRepository;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.impl.cf.FieldValueExtractor;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.*;
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
    private final FieldValueExtractor fieldValueExtractor;
    private final FieldConfigRepository fieldConfigRepository;
    private final ScriptService scriptService;

    public ExtrasResource(
        PermissionHelper permissionHelper,
        ScriptInvalidationService scriptInvalidationService,
        EventListenerRepository listenerRepository,
        FieldValueExtractor fieldValueExtractor,
        FieldConfigRepository fieldConfigRepository,
        ScriptService scriptService
    ) {
        this.permissionHelper = permissionHelper;
        this.scriptInvalidationService = scriptInvalidationService;
        this.listenerRepository = listenerRepository;
        this.fieldValueExtractor = fieldValueExtractor;
        this.fieldConfigRepository = fieldConfigRepository;
        this.scriptService = scriptService;
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
            fieldValueExtractor.clearCache();

            return null;
        }).getResponse();
    }

    @GET
    @Path("/cacheStats")
    @WebSudoRequired
    public Response getCacheStats() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            return scriptService.getCacheStats();
        }).getResponse();
    }
}
