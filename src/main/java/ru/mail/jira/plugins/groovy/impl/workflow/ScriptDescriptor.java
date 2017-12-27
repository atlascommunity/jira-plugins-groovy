package ru.mail.jira.plugins.groovy.impl.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString @AllArgsConstructor
public class ScriptDescriptor {
    private String id;
    private boolean fromRegistry;
    private String scriptBody;
}
