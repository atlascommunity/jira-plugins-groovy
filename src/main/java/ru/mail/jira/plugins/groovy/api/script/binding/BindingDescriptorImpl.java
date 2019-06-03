package ru.mail.jira.plugins.groovy.api.script.binding;

import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;

import javax.annotation.Nonnull;

public class BindingDescriptorImpl<T> implements BindingDescriptor<T> {
    private final T object;
    private final Class<T> type;

    public BindingDescriptorImpl(T object, Class<T> type) {
        this.object = object;
        this.type = type;
    }

    @Override
    public T getValue(ScriptType scriptType, String scriptId) {
        return object;
    }

    @Nonnull
    @Override
    public Class<T> getType() {
        return type;
    }

    @Nonnull
    @Override
    public ClassDoc getDoc() {
        return null;
    }

    @Override
    public void dispose() {

    }
}
