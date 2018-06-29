package ru.mail.jira.plugins.groovy.api.script;

import lombok.Getter;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContext;

@Getter
public class CompiledScript {
    private final Class scriptClass;
    private final ParseContext parseContext;

    public CompiledScript(Class scriptClass, ParseContext parseContext) {
        this.scriptClass = scriptClass;
        this.parseContext = parseContext;
    }
}
