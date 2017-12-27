package ru.mail.jira.plugins.groovy.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.templaterenderer.TemplateRenderer;

@Scanned
public class ListenersServlet extends AbstractTemplateServlet {
    protected ListenersServlet(
        @ComponentImport TemplateRenderer templateRenderer,
        @ComponentImport WebSudoManager webSudoManager
    ) {
        super(
            templateRenderer,
            webSudoManager,
            "ru/mail/jira/plugins/groovy/templates/listeners.vm"
        );
    }
}
