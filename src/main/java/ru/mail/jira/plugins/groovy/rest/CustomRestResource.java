package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import ru.mail.jira.plugins.groovy.api.RestRepository;
import ru.mail.jira.plugins.groovy.api.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.RestScriptDto;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;

@Scanned
@Path("/custom/{scriptKey}")
@AnonymousAllowed
public class CustomRestResource {
    private final JiraAuthenticationContext authenticationContext;
    private final RestRepository restRepository;
    private final ScriptService scriptService;

    public CustomRestResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        RestRepository restRepository,
        ScriptService scriptService
    ) {
        this.authenticationContext = authenticationContext;
        this.restRepository = restRepository;
        this.scriptService = scriptService;
    }

    @GET
    public Response customRestGet(
        @PathParam("scriptKey") String scriptKey,
        @Context UriInfo uriInfo
    ) throws Exception {
        return runScript(HttpMethod.GET, scriptKey, uriInfo, null);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response customRestPost(
        @PathParam("scriptKey") String scriptKey,
        @Context UriInfo uriInfo,
        String body
    ) throws Exception {
        return runScript(HttpMethod.POST, scriptKey, uriInfo, body);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response customRestPut(
        @PathParam("scriptKey") String scriptKey,
        @Context UriInfo uriInfo,
        String body
    ) throws Exception {
        return runScript(HttpMethod.PUT, scriptKey, uriInfo, body);
    }

    @DELETE
    @Consumes(MediaType.TEXT_PLAIN)
    public Response customRestDelete(
        @PathParam("scriptKey") String scriptKey,
        @Context UriInfo uriInfo,
        String body
    ) throws Exception {
        return runScript(HttpMethod.DELETE, scriptKey, uriInfo, body);
    }

    private Response runScript(String method, String key, UriInfo uriInfo, String body) throws Exception {
        RestScriptDto script = restRepository.getScript(method, key);

        //todo: execution tracker

        HashMap<String, Object> bindings = new HashMap<>();
        bindings.put("method", method);
        bindings.put("uriInfo", uriInfo);
        bindings.put("body", body);
        bindings.put("user", authenticationContext.getLoggedInUser());

        Response response = (Response) scriptService.executeScript(
            script.getId(),
            script.getScript(),
            ScriptType.REST,
            bindings
        );

        if (response == null) {
            response = Response.noContent().build();
        }
        return response;
    }
}
