package ru.mail.jira.plugins.groovy.api.dto.execution;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Getter @Setter
public class ScriptExecutionSummary {
    @XmlElement
    private String url;
    @XmlElement
    private String name;
    @XmlElement
    private EntityType type;
    @XmlElement
    private long errorCount;
    @XmlElement
    private String lastErrorDate;
    @XmlElement
    private long lastErrorTimestamp;
}
