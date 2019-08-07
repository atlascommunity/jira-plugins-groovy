package ru.mail.jira.plugins.groovy.api.dto.admin;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement
@Getter @Setter
public class AdminScriptDto extends AdminScriptForm {
    @XmlElement
    private boolean builtIn;
    @XmlElement
    private String builtInKey;
    @XmlElement
    private String resultWidth;
    @XmlElement
    private List<ScriptParamDto> params;
    @XmlElement
    private Map<String, Object> defaultValues;
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
    @XmlElement
    private Integer warningCount;
}
