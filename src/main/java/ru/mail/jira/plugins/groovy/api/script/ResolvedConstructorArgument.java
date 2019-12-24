package ru.mail.jira.plugins.groovy.api.script;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ResolvedConstructorArgument {
    private ArgumentType argumentType;
    private Object object;

    public enum ArgumentType {
        PLUGIN,
        STANDARD,
        GLOBAL_OBJECT
    }
}
