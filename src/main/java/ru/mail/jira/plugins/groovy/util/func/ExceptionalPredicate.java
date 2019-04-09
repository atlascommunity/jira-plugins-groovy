package ru.mail.jira.plugins.groovy.util.func;

import java.util.function.Predicate;

@FunctionalInterface
public interface ExceptionalPredicate<T> {
    boolean apply(T t) throws Exception;

    static <T> Predicate<T> makeSafe(ExceptionalPredicate<T> exceptionalFunction) {
        return t -> {
            try {
                return exceptionalFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
