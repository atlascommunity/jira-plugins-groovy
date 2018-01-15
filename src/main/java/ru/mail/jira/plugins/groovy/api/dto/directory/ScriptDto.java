package ru.mail.jira.plugins.groovy.api.dto.directory;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter @Setter
@XmlRootElement
public class ScriptDto extends ScriptForm {
    @XmlElement
    private Integer id;
    @XmlElement
    private boolean deleted;
    @XmlElement
    private List<ChangelogDto> changelogs;
    @XmlElement
    private List<ScriptParamDto> params;
}
