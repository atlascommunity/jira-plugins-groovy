package ru.mail.jira.plugins.groovy.api.script.binding;

import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;

import javax.annotation.Nonnull;

public interface BindingDescriptor<T> {
    T getValue(String scriptId);

    @Nonnull
    Class<T> getType();

    @Nonnull
    ClassDoc getDoc();

    void dispose() throws Exception;
}
