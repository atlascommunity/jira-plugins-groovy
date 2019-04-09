package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.util.func.ExceptionalConsumer;
import ru.mail.jira.plugins.groovy.util.ValidationException;
import ru.mail.jira.plugins.groovy.util.func.ExceptionalPredicate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AddFavouriteDashboard implements BuiltInScript<String> {
    private final PortalPageManager portalPageManager;
    private final FavouritesManager<SharedEntity> favouritesManager;

    @Autowired
    public AddFavouriteDashboard(
        @ComponentImport PortalPageManager portalPageManager,
        @ComponentImport FavouritesManager<SharedEntity> favouritesManager
    ) {
        this.portalPageManager = portalPageManager;
        this.favouritesManager = favouritesManager;
    }

    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) {
        List<ApplicationUser> users = (List<ApplicationUser>) params.get("users");
        Long dashboardId = (Long) params.get("dashboardId");

        if (users == null) {
            throw new ValidationException("Users are required");
        }

        if (dashboardId == null) {
            throw new ValidationException("Dashboard id is required");
        }

        PortalPage portalPage = portalPageManager.getPortalPageById(dashboardId);

        if (portalPage == null) {
            throw new ValidationException("Dashboard not found");
        }

        List<ApplicationUser> withoutPermission = users
            .stream()
            .filter(it -> !portalPageManager.hasPermissionToUse(it, portalPage))
            .collect(Collectors.toList());

        if (!withoutPermission.isEmpty()) {
            throw new ValidationException(
                "Some users don't have access to dashboard: " +
                    withoutPermission
                        .stream()
                        .map(ApplicationUser::getName)
                        .collect(Collectors.joining(", "))
            );
        }

        users
            .stream()
            .filter(ExceptionalPredicate.makeSafe(it -> !favouritesManager.isFavourite(it, portalPage)))
            .forEach(ExceptionalConsumer.makeSafe(it -> favouritesManager.addFavourite(it, portalPage)));

        return "done";

    }

    @Override
    public String getKey() {
        return "addFavouriteDashboard";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.addFavouriteDashboard";
    }

    @Override
    public boolean isHtml() {
        return false;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("users", "Users", ParamType.MULTI_USER, false),
            new ScriptParamDto("dashboardId", "Dashboard id", ParamType.LONG, false)
        );
    }
}
