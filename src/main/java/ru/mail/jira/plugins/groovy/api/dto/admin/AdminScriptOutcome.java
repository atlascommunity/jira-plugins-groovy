package ru.mail.jira.plugins.groovy.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Getter @Setter @AllArgsConstructor
public class AdminScriptOutcome<T> {
    @XmlElement
    private boolean success;
    @XmlElement
    private T message;
}
