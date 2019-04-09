package ru.mail.jira.plugins.groovy.util.func;

import java.util.function.Consumer;

@FunctionalInterface
public interface ExceptionalConsumer<T> {
    void apply(T t) throws Exception;

    static <T> Consumer<T> makeSafe(ExceptionalConsumer<T> exceptionalFunction) {
        return t -> {
            try {
                exceptionalFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
