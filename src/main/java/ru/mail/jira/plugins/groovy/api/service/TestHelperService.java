package ru.mail.jira.plugins.groovy.api.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;

public interface TestHelperService {
    ActiveObjects getActiveObjects();

    @Transactional
    void transactional(ApplicationUser user, String directoryName);

    void nonTransactional(ApplicationUser user, String directoryName);

    <T extends Exception> T getCompilationExceptionCause(Exception e);

    Class<?> loadClass(String name) throws ClassNotFoundException;
}
