package ru.mail.jira.plugins.groovy.api.repository;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskForm;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskDto;

import java.util.List;

public interface ScheduledTaskRepository {
    List<ScheduledTaskDto> getAllTasks(boolean includeChangelogs, boolean includeRunInfo);

    ScheduledTaskDto getTaskInfo(int id, boolean includeChangelogs, boolean includeRunInfo);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    ScheduledTaskDto createTask(ApplicationUser user, ScheduledTaskForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    ScheduledTaskDto updateTask(ApplicationUser user, int id, ScheduledTaskForm form);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    void setEnabled(ApplicationUser user, int id, boolean enabled);

    /**
     * All mutations must go through {@link ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService}
     */
    void deleteTask(ApplicationUser user, int id);

    ScheduledTaskDto restoreTask(ApplicationUser user, int id);
}
