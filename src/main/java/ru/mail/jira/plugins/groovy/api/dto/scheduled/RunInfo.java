package ru.mail.jira.plugins.groovy.api.dto.scheduled;

import com.atlassian.scheduler.status.RunOutcome;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class RunInfo {
    @XmlElement
    private String startDate;
    @XmlElement
    private long duration;
    @XmlElement
    private RunOutcome outcome;
    @XmlElement
    private String message;
}
