package ru.mail.jira.plugins.groovy.impl.groovy;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class ParseContext {
    @Setter
    private boolean extended;
    private final List<ScriptParamDto> parameters = new ArrayList<>();
    private final Set<String> plugins = new HashSet<>();
    private final List<ScriptInjection> injections = new ArrayList<>();
    private final Set<Class> completedExtensions = new HashSet<>();
}
