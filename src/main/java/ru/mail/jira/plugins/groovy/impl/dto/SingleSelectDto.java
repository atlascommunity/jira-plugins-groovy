package ru.mail.jira.plugins.groovy.impl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@XmlRootElement
public class SingleSelectDto {
    @XmlElement
    private String label;
    @XmlElement
    private String value;
    @XmlElement(name = "img-src")
    private String imgSrc;
}
