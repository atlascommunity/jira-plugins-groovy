package ru.mail.jira.plugins.groovy.api.dto.console;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@AllArgsConstructor
@XmlRootElement
public class ConsoleResponse {
    @XmlElement
    private String result;
    @XmlElement
    private long time;
}
