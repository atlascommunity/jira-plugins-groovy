package ru.mail.jira.plugins.groovy.impl.var;

public interface GlobalVariable<T> {
    T getValue();

    void dispose() throws Exception;
}
