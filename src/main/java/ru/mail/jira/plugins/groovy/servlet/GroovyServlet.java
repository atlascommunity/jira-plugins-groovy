package ru.mail.jira.plugins.groovy.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import ru.mail.jira.plugins.groovy.impl.PermissionHelper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;

@Scanned
public class GroovyServlet extends HttpServlet {
    private static final Set<String> ALLOWED_RESOURCES = ImmutableSet.of(
        "console",
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
    private final LoginUriProvider loginUriProvider;
    private final PermissionHelper permissionHelper;

    public GroovyServlet(
        @ComponentImport TemplateRenderer templateRenderer,
        @ComponentImport WebSudoManager webSudoManager,
        @ComponentImport LoginUriProvider loginUriProvider,
        PermissionHelper permissionHelper
    ) {
        this.templateRenderer = templateRenderer;
        this.webSudoManager = webSudoManager;
        this.loginUriProvider = loginUriProvider;
        this.permissionHelper = permissionHelper;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (permissionHelper.isAnon()) {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
            return;
        }

        try {
            String path = request.getPathInfo();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if ("custom-field".equals(path)) {
                response.sendRedirect("fields/" + request.getParameter("fieldConfigId") + "/edit");
                return;
            }

            String matchedResource = ALLOWED_RESOURCES.stream().filter(path::startsWith).findAny().orElse("main");

            if (!permissionHelper.isAdmin()) {
                response.sendError(403);
                return;
            }

            webSudoManager.willExecuteWebSudoRequest(request);

            response.setContentType("text/html;charset=utf-8");
            templateRenderer.render("ru/mail/jira/plugins/groovy/templates/" + matchedResource + ".vm", response.getWriter());
        } catch (WebSudoSessionException wes) {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
