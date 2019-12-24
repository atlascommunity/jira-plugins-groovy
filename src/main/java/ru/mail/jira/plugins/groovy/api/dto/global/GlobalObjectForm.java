package ru.mail.jira.plugins.groovy.api.dto.global;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptForm;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Getter @Setter
public class GlobalObjectForm extends ScriptForm {
    private String dependencies;
}
