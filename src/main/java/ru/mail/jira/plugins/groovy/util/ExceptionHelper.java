package ru.mail.jira.plugins.groovy.util;

public final class ExceptionHelper {
    private ExceptionHelper() {}

    public static String writeExceptionToString(Exception e) {
        return e.getMessage(); //todo
    }
}
