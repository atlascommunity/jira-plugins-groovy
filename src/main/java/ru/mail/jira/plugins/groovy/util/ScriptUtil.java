package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.Lists;
import ru.mail.jira.plugins.groovy.api.entity.Script;
import ru.mail.jira.plugins.groovy.api.entity.ScriptDirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptUtil {
    public static String getExpandedName(Script script) {
        return getExpandedName(script.getDirectory()) + "/" + script.getName();
    }

    public static String getExpandedName(ScriptDirectory directory) {
        List<String> nameElements = new ArrayList<>();
        ScriptDirectory dir = directory;
        while (dir != null) {
            nameElements.add(dir.getName());
            dir = dir.getParent();
        }

        return Lists.reverse(nameElements).stream().collect(Collectors.joining("/"));
    }
}
