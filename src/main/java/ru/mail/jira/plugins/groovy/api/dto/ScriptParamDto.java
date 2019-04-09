package ru.mail.jira.plugins.groovy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.script.ParamType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@XmlRootElement
public class ScriptParamDto {
    @XmlElement
    private String name;
    @XmlElement
    private String displayName;
    @XmlElement
    private ParamType paramType;
    @XmlElement
    private boolean optional = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptParamDto that = (ScriptParamDto) o;
        return optional == that.optional &&
            Objects.equals(name, that.name) &&
            Objects.equals(displayName, that.displayName) &&
            paramType == that.paramType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName, paramType, optional);
    }
}
