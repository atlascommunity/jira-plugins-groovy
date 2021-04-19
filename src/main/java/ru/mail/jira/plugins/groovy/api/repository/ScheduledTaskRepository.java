package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskForm;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskDto;

import java.util.List;

public interface ScheduledTaskRepository {
    List<ScheduledTaskDto> getAllTasks(boolean includeChangelogs, boolean includeRunInfo);

    List<ChangelogDto> getChangelogs(int id);

    ScheduledTaskDto getTaskInfo(int id, boolean includeChangelogs, boolean includeRunInfo);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    @Transactional
    ScheduledTaskDto createTask(ApplicationUser user, ScheduledTaskForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    @Transactional
    ScheduledTaskDto updateTask(ApplicationUser user, int id, ScheduledTaskForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    @Transactional
    void setEnabled(ApplicationUser user, int id, boolean enabled);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    @Transactional
    void deleteTask(ApplicationUser user, int id);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    @Transactional
    ScheduledTaskDto restoreTask(ApplicationUser user, int id);
}
