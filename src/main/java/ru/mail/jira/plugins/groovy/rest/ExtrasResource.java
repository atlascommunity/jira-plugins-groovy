package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import ru.mail.jira.plugins.groovy.api.EventListenerRepository;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Scanned
@Path("/extras")
public class ExtrasResource {
    private final PermissionHelper permissionHelper;
    private final ScriptInvalidationService scriptInvalidationService;
    private final EventListenerRepository listenerRepository;

    public ExtrasResource(
        PermissionHelper permissionHelper,
        ScriptInvalidationService scriptInvalidationService,
        EventListenerRepository listenerRepository
    ) {
        this.permissionHelper = permissionHelper;
        this.scriptInvalidationService = scriptInvalidationService;
        this.listenerRepository = listenerRepository;
    }

    @POST
    @Path("/clearCache")
    @WebSudoRequired
    public Response invalidateCaches() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            scriptInvalidationService.invalidateAll();
            listenerRepository.invalidate();

            return null;
        }).getResponse();
    }
}
