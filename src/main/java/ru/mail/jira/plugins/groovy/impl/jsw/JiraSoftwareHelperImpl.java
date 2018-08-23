package ru.mail.jira.plugins.groovy.impl.jsw;

import com.atlassian.greenhopper.api.customfield.ManagedCustomFieldsService;
import com.atlassian.greenhopper.api.issuetype.ManagedIssueTypesService;
import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.Page;
import com.atlassian.greenhopper.service.PageRequests;
import com.atlassian.greenhopper.service.ServiceOutcome;
import com.atlassian.greenhopper.service.rapid.RapidViewQueryService;
import com.atlassian.greenhopper.service.rapid.view.RapidViewQuery;
import com.atlassian.greenhopper.service.rapid.view.RapidViewService;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.greenhopper.service.sprint.SprintQuery;
import com.atlassian.greenhopper.service.sprint.SprintService;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.query.Query;

import java.util.Collection;
import java.util.EnumSet;

@Scanned
public class JiraSoftwareHelperImpl implements JiraSoftwareHelper {
    private final ManagedIssueTypesService managedIssueTypesService;
    private final ManagedCustomFieldsService managedCustomFieldsService;
    private final RapidViewService rapidViewService;
    private final RapidViewQueryService rapidViewQueryService;
    private final SprintService sprintService;

    public JiraSoftwareHelperImpl(
        ManagedIssueTypesService managedIssueTypesService,
        ManagedCustomFieldsService managedCustomFieldsService,
        RapidViewService rapidViewService,
        RapidViewQueryService rapidViewQueryService,
        SprintService sprintService
    ) {
        this.managedIssueTypesService = managedIssueTypesService;
        this.managedCustomFieldsService = managedCustomFieldsService;
        this.rapidViewService = rapidViewService;
        this.rapidViewQueryService = rapidViewQueryService;
        this.sprintService = sprintService;
    }

    public boolean isAvailable() {
        return true;
    }

    @Override
    public CustomField getEpicLinkField() {
        return managedCustomFieldsService.getEpicLinkCustomField().get();
    }

    @Override
    public CustomField getRankField() {
        return managedCustomFieldsService.getRankCustomField().get();
    }

    @Override
    public CustomField getSprintField() {
        return managedCustomFieldsService.getSprintCustomField().get();
    }

    @Override
    public IssueType getEpicIssueType() {
        return managedIssueTypesService.getEpicIssueType().get();
    }

    @Override
    public RapidView findRapidViewByName(ApplicationUser user, String name) {
        ServiceOutcome<Page<RapidView>> outcome = rapidViewService.getRapidViews(
            user, PageRequests.all(),
            RapidViewQuery.builder().types(EnumSet.of(RapidView.Type.SCRUM)).build()
        );

        if (outcome.isValid()) {
            Page<RapidView> page = outcome.get();

            return page.getValues().stream().filter(it -> it.getName().equals(name)).findAny().orElse(null);
        }

        return null;
    }

    @Override
    public Query getRapidViewQuery(ApplicationUser user, RapidView rapidView) {
        return rapidViewQueryService.getRapidViewQuery(user, rapidView).get();
    }

    @Override
    public Collection<Sprint> findActiveSprintsByBoard(ApplicationUser user, RapidView rapidView) {
        return sprintService.getSprints(
            user, rapidView,
            PageRequests.all(),
            SprintQuery.builder().states(EnumSet.of(Sprint.State.ACTIVE)).build()
        ).get().getValues();
    }

    @Override
    public Sprint findSprint(ApplicationUser user, RapidView rapidView, String name) {
        return sprintService
            .getSprints(
                user, rapidView,
                PageRequests.all(),
                SprintQuery.builder().build()
            )
            .get()
            .getValues()
            .stream()
            .filter(it -> it.getName().equals(name))
            .findAny()
            .orElse(null);
    }
}
