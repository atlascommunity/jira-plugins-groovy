package ru.mail.jira.plugins.groovy.impl.jsw;

import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.Query;

import java.util.Collection;

public class JiraSoftwareHelperStub implements JiraSoftwareHelper {
    public JiraSoftwareHelperStub() {}

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public CustomField getEpicLinkField() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CustomField getRankField() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CustomField getSprintField() {
        throw  new UnsupportedOperationException();
    }

    @Override
    public IssueType getEpicIssueType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RapidView findRapidViewByName(ApplicationUser user, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query getRapidViewQuery(ApplicationUser user, RapidView rapidView) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Sprint> findActiveSprintsByBoard(ApplicationUser user, RapidView rapidView) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sprint findSprint(ApplicationUser user, RapidView rapidView, String name) {
        throw new UnsupportedOperationException();
    }
}
