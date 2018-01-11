package ru.mail.jira.plugins.groovy.impl.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter @Setter @ToString @AllArgsConstructor
public class ScriptDescriptor {
    private String id;
    private boolean fromRegistry;
    private String scriptBody;
    private Map<String, Object> params;
}
