package ru.mail.jira.plugins.groovy.api.dto.jql;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptForm;

import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class JqlFunctionForm extends ScriptForm {
}
