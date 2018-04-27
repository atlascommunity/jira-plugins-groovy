package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class ScriptForm {
    @XmlElement
    private String name;
    @XmlElement
    private String description;
    @XmlElement
    private String scriptBody;
    @XmlElement
    private String comment;
}
