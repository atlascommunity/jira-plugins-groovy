package ru.mail.jira.plugins.groovy.api.dto.rest;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptForm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@Getter @Setter
@XmlRootElement
public class RestScriptForm extends ScriptForm {
    @XmlElement
    private Set<HttpMethod> methods;
    @XmlElement
    private Set<String> groups;
}
