package ru.mail.jira.plugins.groovy.api.dto.listener;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptForm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class EventListenerForm extends ScriptForm {
    @XmlElement
    private ConditionDescriptor condition;
    @XmlElement
    private boolean alwaysTrack;

    public boolean matches(EventListenerForm other) {
        return super.matches(other)
            && condition.equals(other.condition);
    }
}
