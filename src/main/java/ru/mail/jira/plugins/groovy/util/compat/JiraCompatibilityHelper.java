package ru.mail.jira.plugins.groovy.util.compat;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JiraCompatibilityHelper {
    private final ArchivingHelper archivingHelper;

    @Autowired
    public JiraCompatibilityHelper(
        @ComponentImport BuildUtilsInfo buildUtilsInfo
    ) {
        int[] versionNumbers = buildUtilsInfo.getVersionNumbers();

        if (versionNumbers[0] == 7 && versionNumbers[1] >= 10 || versionNumbers[0] > 7) {
            archivingHelper = new ArchivingHelperImpl();
        } else {
            archivingHelper = new StubArchivingHelper();
        }
    }

    public boolean isProjectArchived(Project project) {
        return archivingHelper.isProjectArchived(project);
    }
}
