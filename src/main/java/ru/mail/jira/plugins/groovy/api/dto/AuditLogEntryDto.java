package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class AuditLogEntryDto extends AuditLogEntryForm {
    @XmlElement
    private Integer id;
    @XmlElement
    private JiraUser user;
}
