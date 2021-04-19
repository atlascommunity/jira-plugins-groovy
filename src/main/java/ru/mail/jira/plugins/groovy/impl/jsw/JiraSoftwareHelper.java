package ru.mail.jira.plugins.groovy.impl.jsw;

import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.Query;

import java.util.Collection;
import java.util.Optional;

public interface JiraSoftwareHelper {
    CustomField getEpicLinkField();

    CustomField getRankField();

    CustomField getSprintField();

    IssueType getEpicIssueType();

    Optional<RapidView> findRapidViewByName(ApplicationUser user, String name);

    Query getRapidViewQuery(ApplicationUser user, RapidView rapidView);

    Collection<Sprint> findActiveSprintsByBoard(ApplicationUser user, RapidView rapidView);

    Optional<Sprint> findSprint(ApplicationUser user, RapidView rapidView, String name);
}
