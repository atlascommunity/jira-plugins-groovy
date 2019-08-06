package ru.mail.jira.plugins.groovy.api.script.binding;

import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import javax.annotation.Nonnull;

public interface BindingDescriptor<T> {
    T getValue(ScriptType type, String scriptId);

    @Nonnull
    Class<T> getType();

    @Nonnull
    ClassDoc getDoc();

    void dispose() throws Exception;
}
