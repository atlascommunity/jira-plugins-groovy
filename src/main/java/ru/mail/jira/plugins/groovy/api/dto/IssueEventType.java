package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class IssueEventType {
    @XmlElement
    private Long id;
    @XmlElement
    private String name;
}
