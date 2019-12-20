package ru.mail.jira.plugins.groovy.api.service;

import ru.mail.jira.plugins.groovy.api.script.CompiledScript;

public interface SingletonFactory {
    <T> Object[] getConstructorArguments(CompiledScript<T> type) throws IllegalArgumentException;

    <T> T createInstance(CompiledScript<T> type);
}
