package ru.mail.jira.plugins.groovy.api.dao;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.api.entity.GlobalObject;
import ru.mail.jira.plugins.groovy.api.entity.GlobalObjectChangelog;

import java.util.List;

public interface GlobalObjectDao {
    List<GlobalObject> getAll();

    GlobalObjectChangelog[] getChangelogs(int id);

    GlobalObject get(int id);

    GlobalObject getByName(String name);

    @Transactional
    GlobalObject createScript(ApplicationUser user, GlobalObjectForm form);

    @Transactional
    GlobalObject updateScript(ApplicationUser user, int id, GlobalObjectForm form);

    @Transactional
    void deleteScript(ApplicationUser user, int id);

    @Transactional
    void restoreScript(ApplicationUser user, int id);
}
