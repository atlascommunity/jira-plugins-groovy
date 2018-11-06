package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.util.RestExecutor;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Scanned
@Path("/binding")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BindingResource {
    private final PermissionHelper permissionHelper;
    private final ScriptService scriptService;

    public BindingResource(
        PermissionHelper permissionHelper,
        ScriptService scriptService
    ) {
        this.permissionHelper = permissionHelper;
        this.scriptService = scriptService;
    }

    @GET
    @Path("/all")
    public Response getBindingDocs() {
        return new RestExecutor<>(() -> {
            permissionHelper.checkIfAdmin();

            Map<String, BindingDescriptor> bindings = scriptService.getGlobalBindings();
            Map<String, ClassDoc> docs = new HashMap<>();

            bindings.forEach((k, v) -> docs.put(k, v.getDoc()));

            return docs;
        }).getResponse();
    }
}
