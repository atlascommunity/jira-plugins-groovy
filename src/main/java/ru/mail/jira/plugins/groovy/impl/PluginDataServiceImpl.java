package ru.mail.jira.plugins.groovy.impl;

import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.service.PluginDataService;

import java.util.Optional;

@Component
public class PluginDataServiceImpl implements PluginDataService {
    @Override
    public boolean isSentryEnabled() {
        return true;
    }

    @Override
    public void setSentryEnabled(boolean enabled) {
        //todo
    }

    @Override
    public String getSentryDsnValue() {
        return null;
    }

    @Override
    public Optional<String> getSentryDsn() {
        return Optional.of("");
    }

    @Override
    public void setSentryDsn(String dsn) {
        //todo
    }
}
