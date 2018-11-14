package ru.mail.jira.plugins.groovy.api.dto.global;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@Getter @Setter
public class GlobalObjectDto extends GlobalObjectForm {
    @XmlElement
    private int id;
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
