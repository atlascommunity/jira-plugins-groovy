package ru.mail.jira.plugins.groovy.impl.listener.condition;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
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
    private Set<Long> entityIds;
    @XmlElement
    private List<ConditionDescriptor> children;
}
