package ru.mail.jira.plugins.groovy.servlet;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Scanned
public abstract class AbstractTemplateServlet extends HttpServlet {
    private final TemplateRenderer templateRenderer;
    private final WebSudoManager webSudoManager;
    private final PermissionHelper permissionHelper;

    private final String template;

    protected AbstractTemplateServlet(
        @ComponentImport TemplateRenderer templateRenderer,
        @ComponentImport WebSudoManager webSudoManager,
        PermissionHelper permissionHelper,
        String template
    ) {
        this.templateRenderer = templateRenderer;
        this.webSudoManager = webSudoManager;
        this.permissionHelper = permissionHelper;
        this.template = template;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            permissionHelper.checkIfAdmin();

            webSudoManager.willExecuteWebSudoRequest(req);

            resp.setContentType("text/html;charset=utf-8");
            templateRenderer.render(template, resp.getWriter());
        } catch (WebSudoSessionException wes) {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }
}