package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskForm;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskDto;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.TaskResult;

public interface ScheduledTaskService {
    ScheduledTaskDto createTask(ApplicationUser user, ScheduledTaskForm form);

    ScheduledTaskDto updateTask(ApplicationUser user, int id, ScheduledTaskForm form);

    void deleteTask(ApplicationUser user, int id);

    void restoreTask(ApplicationUser user, int id);

    void setEnabled(ApplicationUser user, int id, boolean enabled);

    TaskResult runNow(ApplicationUser user, int id);
}
