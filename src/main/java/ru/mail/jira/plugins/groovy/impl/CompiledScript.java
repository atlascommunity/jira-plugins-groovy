package ru.mail.jira.plugins.groovy.impl;

import lombok.Getter;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;

@Getter
class CompiledScript {
    private final Class scriptClass;
    private final ParseContext parseContext;

    CompiledScript(Class scriptClass, ParseContext parseContext) {
        this.scriptClass = scriptClass;
        this.parseContext = parseContext;
    }
}
