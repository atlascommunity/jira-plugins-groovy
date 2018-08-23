package ru.mail.jira.plugins.groovy.impl.jsw;

import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.Query;

import java.util.Collection;

public interface JiraSoftwareHelper {
    boolean isAvailable();

    CustomField getEpicLinkField();

    CustomField getRankField();

    CustomField getSprintField();

    IssueType getEpicIssueType();

    RapidView findRapidViewByName(ApplicationUser user, String name);

    Query getRapidViewQuery(ApplicationUser user, RapidView rapidView);

    Collection<Sprint> findActiveSprintsByBoard(ApplicationUser user, RapidView rapidView);

    Sprint findSprint(ApplicationUser user, RapidView rapidView, String name);
}
