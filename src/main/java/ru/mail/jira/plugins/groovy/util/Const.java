package ru.mail.jira.plugins.groovy.util;

import org.codehaus.jackson.type.TypeReference;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import java.util.List;

public final class Const {
    private Const() {
    }

    public static final String WF_INLINE_SCRIPT = "INLINE_SCRIPT";
    public static final String WF_INLINE_SCRIPT_NAME = "INLINE_SCRIPT_NAME";
    public static final String WF_REPOSITORY_SCRIPT_ID = "SCRIPT_ID";
    public static final String WF_REPOSITORY_SCRIPT_PARAM_PREFIX = "SCRIPT_PARAM_";
    public static final String WF_UUID = "UUID";

    public static final TypeReference<List<ScriptParamDto>> PARAM_LIST_TYPE_REF = new TypeReference<List<ScriptParamDto>>() {
    };

    public static String getParamKey(String paramName) {
        return WF_REPOSITORY_SCRIPT_PARAM_PREFIX + paramName;
    }
}
