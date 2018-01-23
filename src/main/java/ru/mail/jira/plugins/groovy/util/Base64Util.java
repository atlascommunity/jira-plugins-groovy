package ru.mail.jira.plugins.groovy.util;

import java.util.Base64;

public final class Base64Util {
    private static final String BASE64_PREFIX = "b64_";

    private Base64Util() {};

    public static String encode(String value) {
        if (value == null) {
            return null;
        }

        return BASE64_PREFIX + Base64.getEncoder().encodeToString(value.getBytes());
    }

    public static String decode(String value) {
        if (value == null) {
            return null;
        }

        if (value.startsWith(BASE64_PREFIX)) {
            return new String(Base64.getDecoder().decode(value.substring(BASE64_PREFIX.length())));
        } else {
            return value;
        }
    }
}
