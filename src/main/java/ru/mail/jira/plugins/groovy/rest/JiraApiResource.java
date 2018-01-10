package ru.mail.jira.plugins.groovy.rest;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import ru.mail.jira.plugins.groovy.api.dto.IssueEventType;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;
import ru.mail.jira.plugins.groovy.impl.dto.UserDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Scanned
@Path("/jira-api")
public class JiraApiResource {
    private final JiraAuthenticationContext authenticationContext;
    private final EventTypeManager eventTypeManager;
    private final UserSearchService userSearchService;
    private final AvatarService avatarService;
    private final PermissionHelper permissionHelper;

    public JiraApiResource(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        @ComponentImport EventTypeManager eventTypeManager,
        @ComponentImport UserSearchService userSearchService,
        @ComponentImport AvatarService avatarService,
        PermissionHelper permissionHelper
    ) {
        this.authenticationContext = authenticationContext;
        this.eventTypeManager = eventTypeManager;
        this.userSearchService = userSearchService;
        this.avatarService = avatarService;
        this.permissionHelper = permissionHelper;
    }

    @GET
    @Path("/eventType")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IssueEventType> getIssueEventTypes() {
        permissionHelper.checkIfAdmin();

        return eventTypeManager
            .getEventTypes()
            .stream()
            .map(JiraApiResource::mapEventType)
            .collect(Collectors.toList());
    }

    @GET
    @Path("/userPicker")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDto> userPicker(@QueryParam("q") String query) {
        permissionHelper.checkIfAdmin();

        ApplicationUser currentUser = authenticationContext.getLoggedInUser();
        return userSearchService
            .findUsers(
                new JiraServiceContextImpl(currentUser),
                query,
                UserSearchParams
                    .builder(UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY)
                    .maxResults(20)
                    .build()
            )
            .stream()
            .map(user -> new UserDto(
                user.getDisplayName(),
                user.getKey(),
                avatarService.getAvatarURL(currentUser, user, Avatar.Size.SMALL).toString()
            ))
            .collect(Collectors.toList());
    }

    private static IssueEventType mapEventType(EventType type) {
        IssueEventType issueEventType = new IssueEventType();
        issueEventType.setId(type.getId());
        issueEventType.setName(type.getName());
        return issueEventType;
    }
}
