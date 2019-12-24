package ru.mail.jira.plugins.groovy.api.service;

import ru.mail.jira.plugins.groovy.api.script.CompiledScript;
import ru.mail.jira.plugins.groovy.api.script.ResolvedConstructorArgument;

public interface SingletonFactory {
    <T> Object[] getConstructorArguments(CompiledScript<T> type) throws IllegalArgumentException;

    <T> ResolvedConstructorArgument[] getExtendedConstructorArguments(CompiledScript<T> type) throws IllegalArgumentException;

    <T> T createInstance(CompiledScript<T> type);
}
