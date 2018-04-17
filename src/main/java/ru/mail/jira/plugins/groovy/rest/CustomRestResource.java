package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
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
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

@Scanned
@Path("/custom/{scriptKey}")
@Produces(MediaType.APPLICATION_JSON)
@AnonymousAllowed
public class CustomRestResource {
    private final Logger logger = LoggerFactory.getLogger(CustomRestResource.class);

    private final JiraAuthenticationContext authenticationContext;
    private final GroupManager groupManager;
    private final RestRepository restRepository;
    private final ScriptService scriptService;
    private final ExecutionRepository executionRepository;

    public CustomRestResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        @ComponentImport GroupManager groupManager,
        RestRepository restRepository,
        ScriptService scriptService,
        ExecutionRepository executionRepository
    ) {
        this.authenticationContext = authenticationContext;
        this.groupManager = groupManager;
        this.restRepository = restRepository;
        this.scriptService = scriptService;
        this.executionRepository = executionRepository;
    }

    @GET
    public Response customRestGet(
        @PathParam("scriptKey") String scriptKey,
        @Context HttpHeaders headers,
        @Context UriInfo uriInfo
    ) throws Exception {
        return runScript(HttpMethod.GET, scriptKey, headers, uriInfo, null);
    }

    @POST
    @Consumes
    public Response customRestPost(
        @PathParam("scriptKey") String scriptKey,
        @Context HttpHeaders headers,
        @Context UriInfo uriInfo,
        String body
    ) throws Exception {
        return runScript(HttpMethod.POST, scriptKey, headers, uriInfo, body);
    }

    @PUT
    @Consumes
    public Response customRestPut(
        @PathParam("scriptKey") String scriptKey,
        @Context HttpHeaders headers,
        @Context UriInfo uriInfo,
        String body
    ) throws Exception {
        return runScript(HttpMethod.PUT, scriptKey, headers, uriInfo, body);
    }

    @DELETE
    @Consumes
    public Response customRestDelete(
        @PathParam("scriptKey") String scriptKey,
        @Context HttpHeaders headers,
        @Context UriInfo uriInfo,
        String body
    ) throws Exception {
        return runScript(HttpMethod.DELETE, scriptKey, headers, uriInfo, body);
    }

    private Response runScript(HttpMethod method, String key, HttpHeaders headers, UriInfo uriInfo, String body) throws Exception {
        Script script = restRepository.getScript(method, key);

        if (script == null) {
            return Response.status(404).build();
        }

        ApplicationUser user = authenticationContext.getLoggedInUser();

        Set<String> groups = script.getGroupNames();
        if (!groups.isEmpty()) {
            if (user == null || groupManager.getGroupNamesForUser(user).stream().noneMatch(groups::contains)) {
                return Response.status(403).build();
            }
        }

        long t = System.currentTimeMillis();

        boolean successful = true;
        String error = null;
        Exception exception = null;

        HashMap<String, Object> bindings = new HashMap<>();
        bindings.put("method", method);
        bindings.put("uriInfo", uriInfo);
        bindings.put("headers", headers);
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
            ImmutableMap.<String, String>builder()
                .put("method", method.name())
                .put("queryParameters", Objects.toString(uriInfo.getQueryParameters()))
                .put("headers", Objects.toString(headers.getRequestHeaders()))
                .put("body", body != null ? body : "")
                .put("user", user != null ? user.getKey() : "anonymous")
                .put("type", ScriptType.REST.name())
                .build()
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
