package ru.mail.jira.plugins.groovy.impl.workflow;

import lombok.*;

import java.util.Map;

@Getter @Setter
@ToString
@AllArgsConstructor @NoArgsConstructor
public class ScriptDescriptor {
    private String id;
    private String uuid;
    private boolean fromRegistry;
    private String scriptBody;
    private Map<String, Object> params;
}
