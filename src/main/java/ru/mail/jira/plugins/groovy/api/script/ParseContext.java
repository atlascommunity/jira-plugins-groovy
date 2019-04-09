package ru.mail.jira.plugins.groovy.api.script;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.statik.WarningMessage;

import java.util.*;

@Getter
public class ParseContext {
    @Setter
    private boolean compileStatic;
    @Setter
    private boolean extended;
    @Setter
    private Map<String, Class> types;
    @Setter
    private List<WarningMessage> warnings;
    private final List<ScriptParamDto> parameters = new ArrayList<>();
    private final Set<String> plugins = new HashSet<>();
    private final List<ScriptInjection> injections = new ArrayList<>();
    private final Set<Class> completedExtensions = new HashSet<>();
}
