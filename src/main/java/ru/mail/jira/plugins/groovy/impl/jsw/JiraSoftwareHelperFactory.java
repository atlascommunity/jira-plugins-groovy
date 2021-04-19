package ru.mail.jira.plugins.groovy.impl.jsw;

import java.util.Optional;

public interface JiraSoftwareHelperFactory {
    Optional<JiraSoftwareHelper> get();
}
