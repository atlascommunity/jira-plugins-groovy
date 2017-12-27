package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.impl.groovy.ScriptInjection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter @Setter
@XmlRootElement
public class ScriptRequest {
    @XmlElement
    private String id;
    @XmlElement
    private String script;
}
