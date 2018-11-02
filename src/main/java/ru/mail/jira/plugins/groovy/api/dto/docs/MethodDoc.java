package ru.mail.jira.plugins.groovy.api.dto.docs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@ToString @EqualsAndHashCode
@XmlRootElement
public class MethodDoc {
    private final String name;
    private final String description;
    private final TypeDoc returnType;
    private final List<ParameterDoc> parameters;

    public MethodDoc(String name, String description, TypeDoc returnType, List<ParameterDoc> parameters) {
        this.name = name;
        this.description = description;
        this.returnType = returnType;
        this.parameters = parameters;
    }
}
