package ru.mail.jira.plugins.groovy.api.dto.execution;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonRawValue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class ScriptExecutionDto {
    @XmlElement
    private int id;
    @XmlElement
    private String scriptId;
    @XmlElement
    private long time;
    @XmlElement
    private boolean success;
    @XmlElement
    private boolean slow;
    @XmlElement
    private String date;
    @XmlElement
    private String error;
    @XmlElement
    private String log;
    @XmlElement @JsonRawValue
    private String extraParams;
}
