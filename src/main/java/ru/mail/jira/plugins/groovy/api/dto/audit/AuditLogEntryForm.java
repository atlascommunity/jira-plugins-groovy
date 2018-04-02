package ru.mail.jira.plugins.groovy.api.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.entity.EntityAction;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@XmlRootElement
public class AuditLogEntryForm {
    @XmlElement
    private EntityType category;
    @XmlElement
    private Integer entityId;
    @XmlElement
    private EntityAction action;
    @XmlElement
    private String description;
}
