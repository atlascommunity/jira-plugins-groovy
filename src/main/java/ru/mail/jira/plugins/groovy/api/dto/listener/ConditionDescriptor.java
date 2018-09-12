package ru.mail.jira.plugins.groovy.api.dto.listener;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@Getter @Setter
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionDescriptor {
    @XmlElement
    private ConditionType type;
    @XmlElement
    private String className;
    @XmlElement
    private Set<Long> projectIds;
    @XmlElement
    private Set<Long> typeIds;

    private Class classInstance;
}
