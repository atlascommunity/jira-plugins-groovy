package ru.mail.jira.plugins.groovy.impl.var;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;

import javax.annotation.Nonnull;

public class LoggerBindingDescriptor implements BindingDescriptor<Logger> {
    private final Logger defaultLogger = LoggerFactory.getLogger("ru.mail.jira.plugins.groovy.script.$script$");

    @Override
    public Logger getValue(String scriptId) {
        if (scriptId != null) {
            return LoggerFactory.getLogger("ru.mail.jira.plugins.groovy.script." + scriptId);
        } else {
            return defaultLogger;
        }
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
