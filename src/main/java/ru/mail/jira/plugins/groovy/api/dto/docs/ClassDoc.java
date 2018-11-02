package ru.mail.jira.plugins.groovy.api.dto.docs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@ToString @EqualsAndHashCode
@XmlRootElement
public class ClassDoc {
    @XmlElement
    public final String description;
    @XmlElement
    public final List<MethodDoc> methods;

    public ClassDoc(String description, List<MethodDoc> methods) {
        this.description = description;
        this.methods = methods;
    }
}
