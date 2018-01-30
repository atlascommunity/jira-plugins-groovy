package ru.mail.jira.plugins.groovy.api.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.entity.AuditAction;
import ru.mail.jira.plugins.groovy.api.entity.AuditCategory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@XmlRootElement
public class AuditLogEntryForm {
    @XmlElement
    private AuditCategory category;
    @XmlElement
    private AuditAction action;
    @XmlElement
    private String description;
}
