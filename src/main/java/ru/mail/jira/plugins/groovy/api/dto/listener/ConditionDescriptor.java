package ru.mail.jira.plugins.groovy.api.dto.listener;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;
import java.util.Set;

@Getter @Setter
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionDescriptor {
    @XmlElement
    private ConditionType type;
    @XmlElement
    private String className;
    @XmlElement
    private String pluginKey;
    @XmlElement
    private Set<Long> projectIds;
    @XmlElement
    private Set<Long> typeIds;

    private Class classInstance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConditionDescriptor that = (ConditionDescriptor) o;
        return type == that.type &&
            Objects.equals(className, that.className) &&
            Objects.equals(projectIds, that.projectIds) &&
            Objects.equals(typeIds, that.typeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, className, projectIds, typeIds);
    }
}
