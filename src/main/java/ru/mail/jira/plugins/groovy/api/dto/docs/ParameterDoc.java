package ru.mail.jira.plugins.groovy.api.dto.docs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@ToString @EqualsAndHashCode
@XmlRootElement
public class ParameterDoc {
    @XmlElement
    private final TypeDoc type;
    @XmlElement
    private final String name;

    public ParameterDoc(TypeDoc type, String name) {
        this.type = type;
        this.name = name;
    }
}
