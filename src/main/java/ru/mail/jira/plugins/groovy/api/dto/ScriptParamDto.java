package ru.mail.jira.plugins.groovy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.script.ParamType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@XmlRootElement
public class ScriptParamDto {
    @XmlElement
    private String name;
    @XmlElement
    private String displayName;
    @XmlElement
    private ParamType paramType;
    @XmlElement
    private boolean optional = false;
}
