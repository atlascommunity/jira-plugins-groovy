package ru.mail.jira.plugins.groovy.api.script;

public class BindingDescriptorImpl<T> implements BindingDescriptor<T> {
    private final T object;
    private final Class<T> type;

    public BindingDescriptorImpl(T object, Class<T> type) {
        this.object = object;
        this.type = type;
    }

    @Override
    public T getValue(String scriptId) {
        return object;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public void dispose() throws Exception {

    }
}
