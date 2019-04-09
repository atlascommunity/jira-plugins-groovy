package ru.mail.jira.plugins.groovy.api.script.binding;

import java.util.Map;

public interface BindingProvider {
    Map<String, BindingDescriptor<?>> getBindings();
}
