package ru.mail.jira.plugins.groovy.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ChangelogUtil {
    private ChangelogUtil() {}

    public static String generateDiff(int id, String originalName, String name, String originalSource, String newSource) {
        try {
            List<String> originalLines = Arrays.asList(originalSource.split("\n"));
            List<String> newLines = Arrays.asList(newSource.split("\n"));
            Patch<String> patch = DiffUtils.diff(originalLines, newLines);

            return UnifiedDiffUtils
                .generateUnifiedDiff(genName(id, originalName), genName(id, name), originalLines, patch, 5)
                .stream()
                .collect(Collectors.joining("\n"));
        } catch (DiffException e) {
            throw new RuntimeException("Unable to create diff", e);
        }
    }

    private static String genName(int id, String name) {
        return String.valueOf(id) + " - " + name + ".groovy";
    }
}
