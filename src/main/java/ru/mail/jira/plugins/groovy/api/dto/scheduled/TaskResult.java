package ru.mail.jira.plugins.groovy.api.dto.scheduled;

import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.status.RunOutcome;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class TaskResult {
    @XmlElement
    private long time;
    @XmlElement
    private RunOutcome runOutcome;
    @XmlElement
    private String message;

    public static TaskResult fromJobRunnerResponse(JobRunnerResponse response, long time) {
        TaskResult result = new TaskResult();
        result.setTime(time);
        result.setRunOutcome(response.getRunOutcome());
        result.setMessage(response.getMessage());
        return result;
    }
}
