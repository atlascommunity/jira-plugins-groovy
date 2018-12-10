package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EpicsOfFunction extends AbstractEpicFunction {
    @Autowired
    public EpicsOfFunction(
        @ComponentImport IssueLinkTypeManager issueLinkTypeManager,
        @ComponentImport PluginAccessor pluginAccessor,
        SearchHelper searchHelper
    ) {
        super(
            issueLinkTypeManager, pluginAccessor, searchHelper,
            "epicsOf", 1
        );
    }

    @Override
    protected Direction getDirection() {
        return Direction.IN;
    }
}
