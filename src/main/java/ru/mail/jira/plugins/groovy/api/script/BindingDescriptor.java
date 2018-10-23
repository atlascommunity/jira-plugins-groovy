package ru.mail.jira.plugins.groovy.api.script;

public interface BindingDescriptor<T> {
    T getValue(String scriptId);

    Class<T> getType();

    void dispose() throws Exception;
}
