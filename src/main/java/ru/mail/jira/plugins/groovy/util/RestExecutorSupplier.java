package ru.mail.jira.plugins.groovy.util;

@FunctionalInterface
public interface RestExecutorSupplier<T> {
    T get() throws Exception;
}
