package ru.mail.jira.plugins.groovy.api.dao;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.directory.ParentForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.RegistryScriptForm;
import ru.mail.jira.plugins.groovy.api.dto.directory.ScriptDirectoryForm;
import ru.mail.jira.plugins.groovy.api.entity.Script;
import ru.mail.jira.plugins.groovy.api.entity.ScriptDirectory;

public interface RegistryDao {
    @Transactional
    ScriptDirectory createDirectory(ApplicationUser user, ScriptDirectoryForm form);

    @Transactional
    ScriptDirectory updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm form);

    @Transactional
    void moveDirectory(ApplicationUser user, int id, ParentForm form);

    @Transactional
    void deleteDirectory(ApplicationUser user, int id);

    @Transactional
    void restoreDirectory(ApplicationUser user, int id);

    @Transactional
    Script createScript(ApplicationUser user, RegistryScriptForm scriptForm, String parameters);

    @Transactional
    Script updateScript(ApplicationUser user, int id, RegistryScriptForm scriptForm, String parameters);

    @Transactional
    void moveScript(ApplicationUser user, int id, ParentForm form);

    @Transactional
    void deleteScript(ApplicationUser user, int id);

    @Transactional
    void restoreScript(ApplicationUser user, int id);
}
