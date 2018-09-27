package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.*;
import ru.mail.jira.plugins.groovy.api.dto.directory.*;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.dto.PickerOption;

import java.util.List;

public interface ScriptRepository {
    List<ScriptDirectoryDto> getAllDirectories();

    List<RegistryScriptDto> getAllScripts();

    PickerResultSet<PickerOption> getAllDirectoriesForPicker();

    ScriptDirectoryDto getDirectory(int id);

    List<ScriptDescription> getAllScriptDescriptions(WorkflowScriptType type);

    @Transactional
    ScriptDirectoryDto createDirectory(ApplicationUser user, ScriptDirectoryForm form);

    @Transactional
    ScriptDirectoryDto updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm form);

    @Transactional
    void deleteDirectory(ApplicationUser user, int id);

    @Transactional
    void restoreDirectory(ApplicationUser user, int id);

    void moveDirectory(ApplicationUser user, int id, ParentForm form);

    RegistryScriptDto getScript(int id, boolean includeChangelogs, boolean expandName, boolean includeErrorCount);

    @Transactional
    RegistryScriptDto createScript(ApplicationUser user, RegistryScriptForm scriptForm);

    @Transactional
    RegistryScriptDto updateScript(ApplicationUser user, int id, RegistryScriptForm scriptForm);

    @Transactional
    void moveScript(ApplicationUser user, int id, ParentForm form);

    @Transactional
    void deleteScript(ApplicationUser user, int id);

    @Transactional
    void restoreScript(ApplicationUser user, int id);

    List<ChangelogDto> getScriptChangelogs(int id);
}
