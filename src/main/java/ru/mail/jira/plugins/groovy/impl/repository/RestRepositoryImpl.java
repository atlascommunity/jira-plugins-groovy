package ru.mail.jira.plugins.groovy.impl.repository;

import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.RestRepository;
import ru.mail.jira.plugins.groovy.api.dto.RestScriptDto;

//todo
@Component
public class RestRepositoryImpl implements RestRepository {
    @Override
    public RestScriptDto getScript(String method, String key) {
        return new RestScriptDto(
            "test",
            "logger.warn(\"${method}, ${uriInfo.getQueryParameters()}, ${body}, ${user}\")"
        );
    }
}
