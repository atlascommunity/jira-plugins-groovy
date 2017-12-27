package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

@Getter @Setter
@XmlRootElement
public class ScriptDirectoryTreeDto {
    @XmlElement
    private Integer id;
    @XmlElement
    private String name;
    @XmlElement
    private List<ScriptDirectoryTreeDto> children;
    @XmlElement
    private Collection<ScriptDto> scripts;
}
