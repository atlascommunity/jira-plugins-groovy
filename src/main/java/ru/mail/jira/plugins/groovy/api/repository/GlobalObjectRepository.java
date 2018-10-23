package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectDto;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;

import java.util.List;

public interface GlobalObjectRepository {
    List<GlobalObjectDto> getAll();
    GlobalObjectDto get(int id);

    GlobalObjectDto create(ApplicationUser user, GlobalObjectForm form);
    GlobalObjectDto update(ApplicationUser user, int id, GlobalObjectForm form);
    void delete(ApplicationUser user, int id);
    void restore(ApplicationUser user, int id);
}
