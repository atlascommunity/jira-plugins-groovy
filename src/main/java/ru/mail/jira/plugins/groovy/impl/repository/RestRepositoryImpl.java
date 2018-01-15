package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.RestRepository;
import ru.mail.jira.plugins.groovy.api.dto.RestScriptDescription;

//todo
@Component
public class RestRepositoryImpl implements RestRepository {
    private final ActiveObjects ao;

    @Autowired
    public RestRepositoryImpl(
        @ComponentImport ActiveObjects ao
    ) {
        this.ao = ao;
    }

    @Override
    public RestScriptDescription getScript(String method, String key) {
        return new RestScriptDescription(
            "test",
            "logger.warn(\"${method}, ${uriInfo.getQueryParameters()}, ${body}, ${user}\")"
        );
    }
}
