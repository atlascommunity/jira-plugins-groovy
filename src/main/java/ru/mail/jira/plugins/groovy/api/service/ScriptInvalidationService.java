package ru.mail.jira.plugins.groovy.api.service;

public interface ScriptInvalidationService {
    @Deprecated
    void invalidate(String scriptId);

    void invalidateAll();

    void invalidateField(long fieldId);

    void invalidateAllFields();

    void invalidateGlobalObjects();
}
