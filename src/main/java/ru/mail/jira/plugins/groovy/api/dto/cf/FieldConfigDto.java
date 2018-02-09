package ru.mail.jira.plugins.groovy.api.dto.cf;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter @Setter
@XmlRootElement
public class FieldConfigDto extends FieldConfigForm {
    @XmlElement
    private Long id;
    @XmlElement
    private Long customFieldId;
    @XmlElement
    private boolean needsTemplate;
    @XmlElement
    private String uuid;
    @XmlElement
    private String customFieldName;
    @XmlElement
    private String contextName;
    @XmlElement
    private List<ChangelogDto> changelogs;
}
