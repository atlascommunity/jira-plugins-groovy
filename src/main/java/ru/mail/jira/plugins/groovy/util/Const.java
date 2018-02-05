package ru.mail.jira.plugins.groovy.util;

import org.codehaus.jackson.type.TypeReference;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import java.util.List;
import java.util.regex.Pattern;

public final class Const {
    private Const() {
    }

    public static final String PLUGIN_KEY = "ru.mail.jira.plugins.groovy";

    public static final String WF_INLINE_SCRIPT = "INLINE_SCRIPT";
    public static final String WF_INLINE_SCRIPT_NAME = "INLINE_SCRIPT_NAME";
    public static final String WF_REPOSITORY_SCRIPT_ID = "SCRIPT_ID";
    public static final String WF_REPOSITORY_SCRIPT_PARAM_PREFIX = "SCRIPT_PARAM_";
    public static final String WF_UUID = "UUID";

    public static final String REGISTRY_CONDITION_KEY = "ru.mail.jira.plugins.groovyregistry-script-condition";
    public static final String REGISTRY_VALIDATOR_KEY = "ru.mail.jira.plugins.groovyregistry-script-validator";
    public static final String REGISTRY_FUNCTION_KEY = "ru.mail.jira.plugins.groovyregistry-script-function";

    public static final String JIRA_WF_FULL_MODULE_KEY = "full.module.key";

    public static final String SCHEDULED_TASK_ID = "SCHEDULED_TASK_ID";

    public static final Pattern REST_NAME_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}");

    public static final TypeReference<List<ScriptParamDto>> PARAM_LIST_TYPE_REF = new TypeReference<List<ScriptParamDto>>() {
    };

    public static String getParamKey(String paramName) {
        return WF_REPOSITORY_SCRIPT_PARAM_PREFIX + paramName;
    }
}
