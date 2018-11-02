package ru.mail.jira.plugins.groovy.api.dto.docs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@ToString @EqualsAndHashCode
@XmlRootElement
public class TypeDoc {
    @XmlElement
    private final String className;
    @XmlElement
    private final String link;

    public TypeDoc(String className) {
        this.className = className;
        this.link = null;
    }

    public TypeDoc(String className, String link) {
        this.className = className;
        this.link = link;
    }
}
