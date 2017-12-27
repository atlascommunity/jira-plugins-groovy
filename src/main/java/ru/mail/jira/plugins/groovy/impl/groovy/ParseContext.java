package ru.mail.jira.plugins.groovy.impl.groovy;

import lombok.Getter;

import java.util.*;

@Getter
public class ParseContext {
    private Set<String> plugins = new HashSet<>();
    private List<ScriptInjection> injections = new ArrayList<>();
}
