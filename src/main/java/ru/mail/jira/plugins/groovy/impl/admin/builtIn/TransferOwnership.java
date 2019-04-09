package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.util.ValidationException;

import java.util.*;

@Component
public class TransferOwnership implements BuiltInScript<String> {
    private final SearchRequestManager searchRequestManager;
    private final PortalPageManager portalPageManager;
    private final FavouritesManager<SharedEntity> favouritesManager;

    @Autowired
    public TransferOwnership(
        @ComponentImport SearchRequestManager searchRequestManager,
        @ComponentImport PortalPageManager portalPageManager,
        @ComponentImport FavouritesManager<SharedEntity> favouritesManager
    ) {
        this.searchRequestManager = searchRequestManager;
        this.portalPageManager = portalPageManager;
        this.favouritesManager = favouritesManager;
    }

    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) {
        List<SearchRequest> searchRequests = new ArrayList<>();
        List<PortalPage> dashboards = new ArrayList<>();

        ApplicationUser fromUser = (ApplicationUser) params.get("fromUser");
        ApplicationUser toUser = (ApplicationUser) params.get("toUser");
        List<Long> filterIds = parseIds((String) params.get("filterIds"));
        List<Long> dashboardIds = parseIds((String) params.get("dashboardIds"));

        for (long filterId : filterIds) {
            SearchRequest searchRequest = searchRequestManager.getSearchRequestById(filterId);

            if (searchRequest != null) {
                searchRequests.add(searchRequest);
            } else {
                throw new ValidationException("Filter with id \"" + filterId + "\" doesn't exist");
            }
        }

        for (long dashboardId : dashboardIds) {
            PortalPage portalPage = portalPageManager.getPortalPageById(dashboardId);

            if (portalPage != null) {
                dashboards.add(portalPage);
            } else {
                throw new ValidationException("Dashboard with id \"" + dashboardId + "\" doesn't exist");
            }
        }

        if (fromUser != null) {
            searchRequests.addAll(searchRequestManager.getAllOwnedSearchRequests(fromUser));
            dashboards.addAll(portalPageManager.getAllOwnedPortalPages(fromUser));
        }

        if (searchRequests.isEmpty() && dashboards.isEmpty()) {
            throw new ValidationException("Unable to find any entity to transfer");
        }

        if (toUser == null) {
            throw new ValidationException("\"To user\" is not specified");
        }

        List<String> updatedInfo = new ArrayList<>();

        for (SearchRequest searchRequest : searchRequests) {
            ApplicationUser oldOwner = searchRequest.getOwner();

            if (!toUser.equals(oldOwner)) {
                searchRequest.setOwner(toUser);
                SearchRequest updated = searchRequestManager.update(searchRequest);
                updatedInfo.add("Filter: " + searchRequest.getId() + " - \"" + searchRequest.getName() + "\"");

                if (!searchRequestManager.hasPermissionToUse(oldOwner, updated)) {
                    favouritesManager.removeFavourite(oldOwner, updated);
                }
            }
        }

        for (PortalPage dashboard : dashboards) {
            ApplicationUser oldOwner = dashboard.getOwner();

            if (!toUser.equals(oldOwner)) {
                PortalPage updated = portalPageManager.update(
                    new PortalPage.Builder().portalPage(dashboard).owner(toUser).build()
                );
                updatedInfo.add("Dashboard: " + dashboard.getId() + " - \"" + dashboard.getName() + "\"");

                if (!portalPageManager.hasPermissionToUse(oldOwner, updated)) {
                    favouritesManager.removeFavourite(oldOwner, updated);
                }
            }
        }

        return String.join("\n", updatedInfo);
    }

    private List<Long> parseIds(String ids) {
        ids = StringUtils.trimToNull(ids);

        if (ids == null) {
            return ImmutableList.of();
        }

        List<Long> list = new ArrayList<>();
        for (String idString : ids.split(",")) {
            idString = StringUtils.trimToNull(idString);
            if (idString != null) {
                Long id = Longs.tryParse(idString);

                if (id == null) {
                    throw new ValidationException("Invalid id value: \"" + idString + "\"");
                }

                list.add(id);
            }
        }
        return list;
    }

    @Override
    public String getKey() {
        return "transferOwnership";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.transferOwnership";
    }

    @Override
    public boolean isHtml() {
        return false;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("fromUser", "From user", ParamType.USER, true),
            new ScriptParamDto("filterIds", "Filter ids", ParamType.STRING, true),
            new ScriptParamDto("dashboardIds", "Dashboard ids", ParamType.STRING, true),
            new ScriptParamDto("toUser", "To user", ParamType.USER, false)
        );
    }
}
