package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

//todo: support multiple types
@XmlRootElement
@Getter @Setter
public class StaticCheckForm {
    @XmlElement
    private ScriptType scriptType;
    @XmlElement
    private String scriptBody;
    @XmlElement
    private Map<String, String> additionalParams;
}
