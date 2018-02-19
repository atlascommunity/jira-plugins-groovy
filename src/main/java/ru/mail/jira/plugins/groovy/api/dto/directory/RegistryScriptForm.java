package ru.mail.jira.plugins.groovy.api.dto.directory;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptForm;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@Getter @Setter
@XmlRootElement
public class RegistryScriptForm extends ScriptForm {
    @XmlElement
    private Integer directoryId;
    @XmlElement
    private Set<WorkflowScriptType> types;
}
