package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@Getter @Setter
@XmlRootElement
public class ChangelogDto {
    @XmlElement
    private int id;
    @XmlElement
    private String comment;
    @XmlElement
    private String diff;
    @XmlElement
    private String templateDiff;
    @XmlElement
    private JiraUser author;
    @XmlElement
    private String date;
    @XmlElement
    private Set<JiraIssueReference> issueReferences;
}
