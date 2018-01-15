package ru.mail.jira.plugins.groovy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class RestScriptDescription {
    private String id;
    private String script;
}
