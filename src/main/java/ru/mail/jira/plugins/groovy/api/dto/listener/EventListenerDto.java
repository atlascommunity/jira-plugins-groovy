package ru.mail.jira.plugins.groovy.api.dto.listener;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class EventListenerDto extends EventListenerForm {
    @XmlElement
    private int id;
    @XmlElement
    private String uuid;
}
