package ru.mail.jira.plugins.groovy.api.dto.audit;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.JiraUser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class AuditLogEntryDto extends AuditLogEntryForm {
    @XmlElement
    private Integer id;
    @XmlElement
    private String date;
    @XmlElement
    private String scriptName;
    @XmlElement
    private String parentName;
    @XmlElement
    private Boolean deleted;
    @XmlElement
    private Integer scriptId;
    @XmlElement
    private JiraUser user;
}
