package ru.mail.jira.plugins.groovy.api.script;

public @interface WithParam {
    String displayName();

    ParamType type();

    boolean optional() default false;
}
