package ru.mail.jira.plugins.groovy.api;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.*;
import ru.mail.jira.plugins.groovy.api.dto.directory.*;

import java.util.List;

public interface ScriptRepository {
    List<ScriptDirectoryTreeDto> getAllDirectories();

    ScriptDirectoryDto getDirectory(int id);

    ScriptDirectoryDto createDirectory(ApplicationUser user, ScriptDirectoryForm directoryForm);

    ScriptDirectoryDto updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm directoryForm);

    void deleteDirectory(ApplicationUser user, int id);

    List<ScriptDto> getAllScripts(boolean includeChangelog);

    List<ScriptDescription> getAllScriptDescriptions();

    ScriptDto getScript(int id, boolean includeChangelogs, boolean expandName);

    ScriptDto createScript(ApplicationUser user, ScriptForm scriptForm);

    ScriptDto updateScript(ApplicationUser user, int id, ScriptForm scriptForm);

    void deleteScript(ApplicationUser user, int id);
}
