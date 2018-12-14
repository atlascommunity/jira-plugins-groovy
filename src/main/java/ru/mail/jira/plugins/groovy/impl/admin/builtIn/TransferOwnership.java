package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableList;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;

import java.util.List;
import java.util.Map;

public class TransferOwnership implements BuiltInScript<String> {
    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) throws Exception {
        return null;
    }

    @Override
    public String getKey() {
        return "";
    }

    @Override
    public String getI18nKey() {
        return null;
    }

    @Override
    public boolean isHtml() {
        return false;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("fromUser", "From user", ParamType.USER, true),
            new ScriptParamDto("filterIds", "Filter ids", ParamType.STRING, true),
            new ScriptParamDto("dashboardIds", "Dashboard ids", ParamType.STRING, true),
            new ScriptParamDto("toUser", "To user", ParamType.USER, false)
        );
    }
}
