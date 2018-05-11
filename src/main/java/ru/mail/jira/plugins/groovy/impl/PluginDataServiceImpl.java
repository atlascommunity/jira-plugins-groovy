package ru.mail.jira.plugins.groovy.impl;

import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.service.PluginDataService;

@Component
public class PluginDataServiceImpl implements PluginDataService {
    @Override
    public boolean isSentryEnabled() {
        return true;
    }

    @Override
    public void setSentryEnabled() {
        //todo
    }

    @Override
    public String getSentryDsn() {
        return "";
    }

    @Override
    public void setSentryDsn() {
        //todo
    }
}
