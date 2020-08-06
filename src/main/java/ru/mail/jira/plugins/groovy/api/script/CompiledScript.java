package ru.mail.jira.plugins.groovy.api.script;

import lombok.Getter;

@Getter
public class CompiledScript<T> {
    private final Class<T> scriptClass;
    private final ParseContext parseContext;

    public CompiledScript(Class<T> scriptClass, ParseContext parseContext) {
        this.scriptClass = scriptClass;
        this.parseContext = parseContext;
    }
}
