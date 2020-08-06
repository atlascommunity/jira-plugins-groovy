package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.Lists;
import ru.mail.jira.plugins.groovy.api.dto.directory.ScriptDirectoryDto;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.entity.Script;
import ru.mail.jira.plugins.groovy.api.entity.ScriptDirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptUtil {
    public static String getExpandedName(Map<Integer, ScriptDirectoryDto> allDirectories, Script script) {
        return getExpandedName(allDirectories, allDirectories.get(script.getDirectory().getID())) + "/" + script.getName();
    }

    public static String getExpandedName(Script script) {
        return getExpandedName(script.getDirectory()) + "/" + script.getName();
    }

    public static String getExpandedName(Map<Integer, ScriptDirectoryDto> allDirectories, ScriptDirectoryDto directory) {
        List<String> nameElements = new ArrayList<>();
        ScriptDirectoryDto dir = directory;
        while (dir != null) {
            nameElements.add(dir.getName());
            dir = allDirectories.get(dir.getParentId());
        }

        return Lists.reverse(nameElements).stream().collect(Collectors.joining("/"));
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

    public static String getPermalink(EntityType entityType, Integer id) {
        if (entityType.isSupportsPermalink() && id != null) {
            String pluginBaseUrl = "/plugins/servlet/my-groovy/";

            switch (entityType) {
                case REGISTRY_SCRIPT:
                    return pluginBaseUrl + "registry/script/view/" + id;
                case CUSTOM_FIELD:
                    return pluginBaseUrl + "fields/" + id + "/view";
                case ADMIN_SCRIPT:
                    return pluginBaseUrl + "admin-scripts/" + id + "/view";
                case LISTENER:
                    return pluginBaseUrl + "listeners/" + id + "/view";
                case REST:
                    return pluginBaseUrl + "rest/" + id + "/view";
                case SCHEDULED_TASK:
                    return pluginBaseUrl + "scheduled/" + id + "/view";
                case JQL_FUNCTION:
                    return pluginBaseUrl + "jql/" + id + "/view";
                case GLOBAL_OBJECT:
                    return pluginBaseUrl + "go/" + id + "/view";
            }

            return null;
        }

        return null;
    }

}
