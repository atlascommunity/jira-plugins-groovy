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
    private final String className;
    @XmlElement
    private final String href;
    @XmlElement
    public final String description;
    @XmlElement
    public final List<MethodDoc> methods;

    public ClassDoc(String className, String href) {
        this.className = className;
        this.href = href;
        this.description = null;
        this.methods = null;
    }

    public ClassDoc(String className, String description, List<MethodDoc> methods) {
        this.className = className;
        this.href = null;
        this.description = description;
        this.methods = methods;
    }
}
