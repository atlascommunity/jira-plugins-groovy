package ru.mail.jira.plugins.groovy.impl.var;

public interface GlobalVariable<T> {
    T getValue(String scriptId);

    Class<T> getType();

    void dispose() throws Exception;
}
