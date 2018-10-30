package ru.mail.jira.plugins.groovy.api.service;

public interface SingletonFactory {
    <T> Object[] getConstructorArguments(Class<T> type) throws IllegalArgumentException;

    <T> T createInstance(Class<T> type);
}
