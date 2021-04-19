package ru.mail.jira.plugins.groovy.impl.var;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;
import ru.mail.jira.plugins.groovy.impl.groovy.ExecutionContextHolder;
import ru.mail.jira.plugins.groovy.impl.groovy.log.ContextAwareScriptLogger;

import javax.annotation.Nonnull;

public class LoggerBindingDescriptor implements BindingDescriptor<Logger> {
    private final Logger defaultLogger = LoggerFactory.getLogger("ru.mail.jira.plugins.groovy.script.$script$");

    private final ExecutionContextHolder executionContextHolder;

    public LoggerBindingDescriptor(ExecutionContextHolder executionContextHolder) {
        this.executionContextHolder = executionContextHolder;
    }

    @Override
    public Logger getValue(ScriptType scriptType, String scriptId) {
        Logger logger;
        if (scriptId != null) {
            logger = LoggerFactory.getLogger("ru.mail.jira.plugins.groovy.script." + scriptId);
        } else {
            logger = defaultLogger;
        }
        return new ContextAwareScriptLogger(logger, executionContextHolder.get().getLogEntries());
    }

    @Nonnull
    @Override
    public Class<Logger> getType() {
        return Logger.class;
    }

    @Nonnull
    @Override
    public ClassDoc getDoc() {
        return new ClassDoc(
            true, getType().getCanonicalName(),
            "https://www.slf4j.org/api/org/slf4j/Logger.html"
        );
    }

    @Override
    public void dispose() {}
}
