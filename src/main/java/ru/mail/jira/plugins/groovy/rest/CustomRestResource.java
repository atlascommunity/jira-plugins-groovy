package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.RestRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.rest.HttpMethod;
import ru.mail.jira.plugins.groovy.api.dto.rest.Script;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Objects;

@Scanned
@Path("/custom/{scriptKey}")
@Produces(MediaType.APPLICATION_JSON)
@AnonymousAllowed
public class CustomRestResource {
    private final Logger logger = LoggerFactory.getLogger(CustomRestResource.class);

    private final JiraAuthenticationContext authenticationContext;
    private final RestRepository restRepository;
    private final ScriptService scriptService;
    private final ExecutionRepository executionRepository;

    public CustomRestResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        RestRepository restRepository,
        ScriptService scriptService,
        ExecutionRepository executionRepository
    ) {
        this.authenticationContext = authenticationContext;
        this.restRepository = restRepository;
        this.scriptService = scriptService;
        this.executionRepository = executionRepository;
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

    private Response runScript(HttpMethod method, String key, UriInfo uriInfo, String body) throws Exception {
        Script script = restRepository.getScript(method, key);

        if (script == null) {
            return Response.status(404).build();
        }

        long t = System.currentTimeMillis();
        ApplicationUser user = authenticationContext.getLoggedInUser();

        boolean successful = true;
        String error = null;
        Exception exception = null;

        HashMap<String, Object> bindings = new HashMap<>();
        bindings.put("method", method);
        bindings.put("uriInfo", uriInfo);
        bindings.put("body", body);
        bindings.put("currentUser", user);

        Response response = null;
        try {
            response = (Response) scriptService.executeScript(
                script.getId(),
                script.getScript(),
                ScriptType.REST,
                bindings
            );
        } catch (Exception e) {
            successful = false;
            exception = e;
            logger.error("Error for rest script {}", key, exception);
            error = ExceptionHelper.writeExceptionToString(e);
        }

        executionRepository.trackInline(
            script.getId(),
            System.currentTimeMillis() - t,
            successful,
            error,
            ImmutableMap.of(
                "method", method.name(),
                "queryParameters", Objects.toString(uriInfo.getQueryParameters()),
                "body", body != null ? body : "",
                "user", user != null ? user.getKey() : "anonymous",
                "type", ScriptType.REST.name()
            )
        );

        if (exception != null) {
            return Response.status(500).entity(ExceptionHelper.getMessageOrClassName(exception)).build();
        }

        if (response == null) {
            response = Response.noContent().build();
        }

        return response;
    }
}
