package ru.mail.jira.plugins.groovy.api.dto.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@XmlRootElement
public class WorkflowActionItem {
    @XmlElement
    private WorkflowScriptType type;
    @XmlElement
    private Integer order;
}
