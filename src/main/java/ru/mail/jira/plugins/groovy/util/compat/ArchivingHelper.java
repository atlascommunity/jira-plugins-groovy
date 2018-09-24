package ru.mail.jira.plugins.groovy.util.compat;

import com.atlassian.jira.project.Project;

public interface ArchivingHelper {
    boolean isProjectArchived(Project project);
}
