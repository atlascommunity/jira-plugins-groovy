package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.issue.fields.DueDateSystemField;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.codehaus.jackson.type.TypeReference;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class Const {
    private Const() {
    }

    public static final String SEARCHER_DATETIME = "com.atlassian.jira.plugin.system.customfieldtypes:datetimerange";
    public static final String SEARCHER_DATE = "com.atlassian.jira.plugin.system.customfieldtypes:daterange";

    public static final Set<Class> SYSTEM_DATE_FIELDS = ImmutableSet.of(
        DueDateSystemField.class
    );

    public static final Map<String, Class> SEARCHER_TYPES = ImmutableMap
        .<String, Class>builder()
        .put("com.atlassian.jira.plugin.system.customfieldtypes:exactnumber", Double.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:numberrange", Double.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher", String.class)
        .put("com.atlassian.jira.plugin.system.customfieldtypes:exacttextsearcher", String.class)
        .put(SEARCHER_DATETIME, Date.class)
        .put(SEARCHER_DATE, Date.class)
        //todo: leave object for now, need to figure out how to provide several types
        .put("com.atlassian.jira.plugin.system.customfieldtypes:userpickergroupsearcher", Object.class)
        .build();

    public static final String PLUGIN_KEY = "ru.mail.jira.plugins.groovy";

    public static final String WF_INLINE_SCRIPT = "INLINE_SCRIPT";
    public static final String WF_INLINE_SCRIPT_NAME = "INLINE_SCRIPT_NAME";
    public static final String WF_REPOSITORY_SCRIPT_ID = "SCRIPT_ID";
    public static final String WF_REPOSITORY_SCRIPT_PARAM_PREFIX = "SCRIPT_PARAM_";
    public static final String WF_UUID = "UUID";

    public static final String INLINE_CONDITION_KEY = "ru.mail.jira.plugins.groovyinline-script-condition";
    public static final String INLINE_VALIDATOR_KEY = "ru.mail.jira.plugins.groovyinline-script-validator";
    public static final String INLINE_FUNCTION_KEY = "ru.mail.jira.plugins.groovyinline-script-function";

    public static final String REGISTRY_CONDITION_KEY = "ru.mail.jira.plugins.groovyregistry-script-condition";
    public static final String REGISTRY_VALIDATOR_KEY = "ru.mail.jira.plugins.groovyregistry-script-validator";
    public static final String REGISTRY_FUNCTION_KEY = "ru.mail.jira.plugins.groovyregistry-script-function";

    public static final String JIRA_WF_FULL_MODULE_KEY = "full.module.key";

    public static final String SCHEDULED_TASK_ID = "SCHEDULED_TASK_ID";

    public static final Pattern REST_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_-]{1,64}");

    public static final int COMMENT_MAX_LENGTH = 200;

    public static final TypeReference<List<ScriptParamDto>> PARAM_LIST_TYPE_REF = new TypeReference<List<ScriptParamDto>>() {
    };

    public static String getParamKey(String paramName) {
        return WF_REPOSITORY_SCRIPT_PARAM_PREFIX + paramName;
    }
}
