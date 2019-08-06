package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.service.PluginDataService;

import java.util.Objects;
import java.util.Optional;

@Component
public class PluginDataServiceImpl implements PluginDataService {
    private static final String SETTING_PREFIX = "ru.mail.jira.plugins.groovy";

    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public PluginDataServiceImpl(
        @ComponentImport PluginSettingsFactory pluginSettingsFactory
    ) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public boolean isSentryEnabled() {
        Object value = pluginSettingsFactory.createGlobalSettings().get(SETTING_PREFIX + ".sentry.enabled");
        return Objects.equals("true", value);
    }

    @Override
    public void setSentryEnabled(boolean enabled) {
        pluginSettingsFactory.createGlobalSettings().put(SETTING_PREFIX + ".sentry.enabled", String.valueOf(enabled));
    }

    @Override
    public String getSentryDsnValue() {
        return (String) pluginSettingsFactory.createGlobalSettings().get(SETTING_PREFIX + ".sentry.dsn");
    }

    @Override
    public Optional<String> getSentryDsn() {
        return Optional.ofNullable((String) pluginSettingsFactory.createGlobalSettings().get(SETTING_PREFIX + ".sentry.dsn"));
    }

    @Override
    public void setSentryDsn(String dsn) {
        pluginSettingsFactory.createGlobalSettings().put(SETTING_PREFIX + ".sentry.dsn", dsn);
    }
}
