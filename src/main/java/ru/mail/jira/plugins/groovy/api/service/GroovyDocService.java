package ru.mail.jira.plugins.groovy.api.service;

import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.util.WithPluginLoader;

public interface GroovyDocService {
    @WithPluginLoader
    ClassDoc parseDocs(String canonicalName, String className, String source) throws Exception;
}
