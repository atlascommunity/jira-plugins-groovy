package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

public interface ScheduledTask extends AbstractScript {
    @NotNull
    String getUuid();
    void setUuid(String uuid);

    @NotNull
    String getScheduleExpression();
    void setScheduleExpression(String scheduleExpression);

    @NotNull
    String getUserKey();
    void setUserKey(String userKey);

    @NotNull
    ScheduledTaskType getType();
    void setType(ScheduledTaskType type);

    @StringLength(StringLength.UNLIMITED)
    String getScriptBody();
    void setScriptBody(String scriptBody);

    @StringLength(StringLength.UNLIMITED)
    String getJql();
    void setJql(String jql);

    String getWorkflow();
    void setWorkflow(String workflow);

    Integer getWorkflowAction();
    void setWorkflowAction(Integer workflowAction);

    Integer getTransitionOptions();
    void setTransitionOptions(Integer transitionOptions);

    @NotNull
    boolean isEnabled();
    void setEnabled(boolean enabled);

    @OneToMany(reverse = "getTask")
    ScheduledTaskChangelog[] getChangelogs();
}
