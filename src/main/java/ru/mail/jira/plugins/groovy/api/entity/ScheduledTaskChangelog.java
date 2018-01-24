package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Table("S_TASK_CHANGELOG")
public interface ScheduledTaskChangelog extends AbstractChangelog {
    @NotNull
    void setTask(ScheduledTask task);
    ScheduledTask getTask();
}
