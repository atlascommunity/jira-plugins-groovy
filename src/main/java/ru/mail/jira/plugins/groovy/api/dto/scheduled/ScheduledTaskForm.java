package ru.mail.jira.plugins.groovy.api.dto.scheduled;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptForm;
import ru.mail.jira.plugins.groovy.api.entity.ScheduledTaskType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class ScheduledTaskForm extends ScriptForm {
    @XmlElement
    private String scheduleExpression;
    @XmlElement
    private ScheduledTaskType type;
    @XmlElement
    private String userKey;
    @XmlElement
    private String issueJql;
    @XmlElement
    private String issueWorkflowName;
    @XmlElement
    private Integer issueWorkflowActionId;
    @XmlElement
    private TransitionOptionsDto transitionOptions;
}
