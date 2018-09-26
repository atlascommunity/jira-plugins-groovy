package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.core.util.ClassLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    private FileUtil() {}

    public static String readExample(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/examples/groovy/" + fileName + ".groovy")));
    }

    public static String readArquillianExample(String fileName) throws IOException {
        return readArquillianFile(fileName + ".groovy");
    }

    public static String readArquillianFile(String fileName) throws IOException {
        try (InputStream in = ClassLoaderUtils.getResourceAsStream(
            fileName, FileUtil.class
        )) {
            byte[] buffer = new byte[in.available()];
            in.read(buffer);

            return new String(buffer);
        }
    }
}
