package ru.mail.jira.plugins.groovy.api.dto.cf;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class PreviewForm {
    @XmlElement
    private FieldConfigForm configForm;
    @XmlElement
    private String issueKey;
}
