package ru.mail.jira.plugins.groovy.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Scanned
public class GroovyServlet extends HttpServlet {
    private static final Set<String> ALLOWED_RESOURCES = ImmutableSet.of(
        "console",
        "registry",
        "listeners",
        "audit",
        "fields",
        "custom-field",
        "rest",
        "scheduled",
        "extras"
    );

    private final TemplateRenderer templateRenderer;
    private final WebSudoManager webSudoManager;
    private final PermissionHelper permissionHelper;

    public GroovyServlet(
        @ComponentImport TemplateRenderer templateRenderer,
        @ComponentImport WebSudoManager webSudoManager,
        PermissionHelper permissionHelper
    ) {
        this.templateRenderer = templateRenderer;
        this.webSudoManager = webSudoManager;
        this.permissionHelper = permissionHelper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if (!ALLOWED_RESOURCES.contains(path)) {
                resp.sendError(404);
            }

            if (!permissionHelper.isAdmin()) {
                resp.sendError(403);
                return;
            }

            webSudoManager.willExecuteWebSudoRequest(req);

            resp.setContentType("text/html;charset=utf-8");
            templateRenderer.render("ru/mail/jira/plugins/groovy/templates/" + path + ".vm", resp.getWriter());
        } catch (WebSudoSessionException wes) {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }
}
