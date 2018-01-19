package ru.mail.jira.plugins.groovy.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

@Scanned
public class CustomFieldServlet extends AbstractTemplateServlet {
    public CustomFieldServlet(
        @ComponentImport TemplateRenderer templateRenderer,
        @ComponentImport WebSudoManager webSudoManager,
        PermissionHelper permissionHelper
    ) {
        super(templateRenderer, webSudoManager, permissionHelper, "ru/mail/jira/plugins/groovy/templates/custom-field.vm");
    }
}
