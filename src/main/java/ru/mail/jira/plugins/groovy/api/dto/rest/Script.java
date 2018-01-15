package ru.mail.jira.plugins.groovy.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Script {
    private String id;
    private String script;
}
