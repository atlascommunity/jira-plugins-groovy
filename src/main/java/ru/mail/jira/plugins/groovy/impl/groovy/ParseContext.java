package ru.mail.jira.plugins.groovy.impl.groovy;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import java.util.*;

@Getter
public class ParseContext {
    @Setter
    private boolean compileStatic;
    @Setter
    private boolean extended;
    @Setter
    private Map<String, Class> types;
    private final List warnings = new ArrayList();
    private final List<ScriptParamDto> parameters = new ArrayList<>();
    private final Set<String> plugins = new HashSet<>();
    private final List<ScriptInjection> injections = new ArrayList<>();
    private final Set<Class> completedExtensions = new HashSet<>();
}
