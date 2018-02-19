package ru.mail.jira.plugins.groovy.api.dto.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@XmlRootElement
public class WorkflowDto {
    @XmlElement
    private String name;
    @XmlElement
    private boolean active;
    @XmlElement
    private boolean hasDraft;
    @XmlElement
    private List<WorkflowActionDto> actions;
}
