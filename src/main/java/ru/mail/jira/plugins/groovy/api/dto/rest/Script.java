package ru.mail.jira.plugins.groovy.api.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @AllArgsConstructor
public class Script {
    private String id;
    private String script;
    private Set<String> groupNames;
}
