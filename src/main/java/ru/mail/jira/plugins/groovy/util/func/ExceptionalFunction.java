package ru.mail.jira.plugins.groovy.util.func;

import java.util.function.Function;

@FunctionalInterface
public interface ExceptionalFunction<T, R> {
    R apply(T t) throws Exception;

    static <T, R> Function<T, R> makeSafe(ExceptionalFunction<T, R> exceptionalFunction) {
        return t -> {
            try {
                return exceptionalFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
