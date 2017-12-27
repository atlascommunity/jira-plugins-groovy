package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import ru.mail.jira.plugins.groovy.impl.listener.condition.ConditionDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Getter @Setter
public class EventListenerForm {
    @XmlElement
    private String name;
    @XmlElement
    private String script;
    @XmlElement
    private ConditionDescriptor condition;
}
