package ru.mail.jira.plugins.groovy.api.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@XmlRootElement
public class CompilationMessage {
    @XmlElement
    private String type;
    @XmlElement
    private String message;
}
