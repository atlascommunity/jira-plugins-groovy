package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@XmlRootElement
public class PickerResultSet<T> {
    @XmlElement
    private final List<T> options;
    @XmlElement
    private final boolean complete;

    public PickerResultSet(List<T> options, boolean complete) {
        this.options = options;
        this.complete = complete;
    }
}
