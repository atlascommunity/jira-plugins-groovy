package ru.mail.jira.plugins.groovy.util;

import java.util.Arrays;
import java.util.Objects;

public final class ObjectUtil {
    private ObjectUtil() {}

    public static boolean allNonNull(Object ...objects) {
        return Arrays.stream(objects).allMatch(Objects::nonNull);
    }
}
