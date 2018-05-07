package ru.mail.jira.plugins.groovy.api.dto.admin;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@Getter @Setter
public class AdminScriptDto extends AdminScriptForm {
    @XmlElement
    private boolean builtIn;
    @XmlElement
    private String builtInKey;
    @XmlElement
    private List<ScriptParamDto> params;
    @XmlElement
    private Integer id;
    @XmlElement
    private String uuid;
    @XmlElement
    private boolean deleted;
    @XmlElement
    private List<ChangelogDto> changelogs;
    @XmlElement
    private Integer errorCount;
}
