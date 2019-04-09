package ru.mail.jira.plugins.groovy.util.compat;

import com.atlassian.jira.project.Project;

public class StubArchivingHelper implements ArchivingHelper {
    @Override
    public boolean isProjectArchived(Project project) {
        return false;
    }
}
