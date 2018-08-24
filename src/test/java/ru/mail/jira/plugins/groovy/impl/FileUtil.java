package ru.mail.jira.plugins.groovy.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    private FileUtil() {}

    public static String readExample(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/examples/groovy/" + fileName + ".groovy")));
    }
}
