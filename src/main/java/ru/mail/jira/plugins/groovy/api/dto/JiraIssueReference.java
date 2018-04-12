package ru.mail.jira.plugins.groovy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement
@Getter @Setter
@AllArgsConstructor
public class JiraIssueReference {
    @XmlElement
    private final String key;
    @XmlElement
    private final String summary;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JiraIssueReference that = (JiraIssueReference) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key);
    }
}
