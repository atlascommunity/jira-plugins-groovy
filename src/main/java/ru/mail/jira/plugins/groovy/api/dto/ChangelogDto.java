package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
    private JiraUser author;
    @XmlElement
    private String date;
}
