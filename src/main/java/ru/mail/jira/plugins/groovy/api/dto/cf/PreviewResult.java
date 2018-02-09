package ru.mail.jira.plugins.groovy.api.dto.cf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@XmlRootElement
public class PreviewResult {
    @XmlElement
    private String htmlResult;
    @XmlElement
    private long time;
}
