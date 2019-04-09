package ru.mail.jira.plugins.groovy.util;

public final class AntlrUtil {
    private AntlrUtil() {}

    public static String unescapeString(String string) {
        if (string.startsWith("'") || string.startsWith("\"")) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }
}
