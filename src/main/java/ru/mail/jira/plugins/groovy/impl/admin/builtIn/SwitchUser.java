package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.AuditingManager;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.web.context.HttpContext;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.util.ValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Component
public class SwitchUser implements BuiltInScript {
    private final HttpContext httpContext;
    private final AuditingManager auditingManager;

    @Autowired
    public SwitchUser(
        @ComponentImport HttpContext httpContext,
        @ComponentImport AuditingManager auditingManager
    ) {
        this.httpContext = httpContext;
        this.auditingManager = auditingManager;
    }

    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) {
        HttpServletRequest request = httpContext.getRequest();
        if (request == null) {
            throw new ValidationException("Request is null");
        }

        ApplicationUser user = (ApplicationUser) params.get("user");

        if (user != null) {
            HttpSession session = request.getSession();
            if (session == null) {
                throw new ValidationException("Session is null");
            }

            auditingManager.store(new RecordRequest(
                AuditingCategory.USER_MANAGEMENT,
                "Switched user to \"" + user.getDisplayName() + "\" (" + user.getName() + ")",
                "",
                currentUser,
                request.getRemoteAddr()
            ));
            session.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
            return "Switched to user \"" + user.getDisplayName() + "\"";
        }

        throw new ValidationException("User not specified");
    }

    @Override
    public String getKey() {
        return "switchUser";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.switchUser";
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("user", "User", ParamType.USER)
        );
    }
}
