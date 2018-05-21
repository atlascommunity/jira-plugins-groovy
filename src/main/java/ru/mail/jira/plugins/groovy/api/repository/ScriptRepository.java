package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.*;
import ru.mail.jira.plugins.groovy.api.dto.directory.*;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.impl.dto.PickerOption;

import java.util.List;

public interface ScriptRepository {
    List<ScriptDirectoryTreeDto> getAllDirectories();

    PickerResultSet<PickerOption> getAllDirectoriesForPicker();

    ScriptDirectoryDto getDirectory(int id);

    List<ScriptDescription> getAllScriptDescriptions(WorkflowScriptType type);

    ScriptDirectoryDto createDirectory(ApplicationUser user, ScriptDirectoryForm form);

    ScriptDirectoryDto updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm form);

    void deleteDirectory(ApplicationUser user, int id);

    void restoreDirectory(ApplicationUser user, int id);

    void moveDirectory(ApplicationUser user, int id, ParentForm form);

    RegistryScriptDto getScript(int id, boolean includeChangelogs, boolean expandName, boolean includeErrorCount);

    RegistryScriptDto createScript(ApplicationUser user, RegistryScriptForm scriptForm);

    RegistryScriptDto updateScript(ApplicationUser user, int id, RegistryScriptForm scriptForm);

    void moveScript(ApplicationUser user, int id, ParentForm form);

    void deleteScript(ApplicationUser user, int id);

    void restoreScript(ApplicationUser user, int id);

    List<ChangelogDto> getScriptChangelogs(int id);
}
