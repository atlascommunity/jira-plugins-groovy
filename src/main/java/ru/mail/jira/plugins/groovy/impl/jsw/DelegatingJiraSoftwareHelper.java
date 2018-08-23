package ru.mail.jira.plugins.groovy.impl.jsw;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.greenhopper.api.customfield.ManagedCustomFieldsService;
import com.atlassian.greenhopper.api.issuetype.ManagedIssueTypesService;
import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.rapid.RapidViewQueryService;
import com.atlassian.greenhopper.service.rapid.view.RapidViewService;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.greenhopper.service.sprint.SprintService;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.ObjectUtil;
import ru.mail.jira.plugins.groovy.util.PluginComponentUtil;

import java.util.Collection;

@Component
@ExportAsService(LifecycleAware.class)
public class DelegatingJiraSoftwareHelper implements LifecycleAware, JiraSoftwareHelper {
    private static final String GREENHOPPER_KEY = "com.pyxis.greenhopper.jira";

    private final Logger logger = LoggerFactory.getLogger(DelegatingJiraSoftwareHelper.class);
    private final EventPublisher eventPublisher;
    private final PluginAccessor pluginAccessor;

    private JiraSoftwareHelper delegate;

    @Autowired
    public DelegatingJiraSoftwareHelper(@ComponentImport EventPublisher eventPublisher, @ComponentImport PluginAccessor pluginAccessor) {
        this.eventPublisher = eventPublisher;
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher.register(this);
    }

    @EventListener
    public void onEvent(PluginEnabledEvent pluginEnabledEvent) {
        Plugin plugin = pluginEnabledEvent.getPlugin();
        if (GREENHOPPER_KEY.equals(plugin.getKey())) {
            logger.info("updating jsw helper delegate");
            createDelegate(plugin);
        }
    }

    @Override
    public void onStart() {
        createDelegate(pluginAccessor.getPlugin(GREENHOPPER_KEY));
    }

    @Override
    public void onStop() {
        this.eventPublisher.unregister(this);
        logger.info("JSW helper destroyed");
    }

    private void createDelegate(Plugin plugin) {
        try {
            Class.forName("com.atlassian.greenhopper.api.issuetype.ManagedIssueTypesService"); //check if greenhopper classes are available

            logger.info("Creating JSW helper");
            ManagedIssueTypesService managedIssueTypesService = PluginComponentUtil.getPluginComponent(plugin, ManagedIssueTypesService.class);
            ManagedCustomFieldsService managedCustomFieldsService = PluginComponentUtil.getPluginComponent(plugin, ManagedCustomFieldsService.class);
            RapidViewService rapidViewService = PluginComponentUtil.getPluginComponent(plugin, RapidViewService.class);
            RapidViewQueryService rapidViewQueryService = PluginComponentUtil.getPluginComponent(plugin, RapidViewQueryService.class);
            SprintService sprintService = PluginComponentUtil.getPluginComponent(plugin, SprintService.class);

            if (ObjectUtil.allNonNull(
                managedIssueTypesService,
                managedCustomFieldsService,
                rapidViewService,
                rapidViewQueryService,
                sprintService
            )) {
                this.delegate = new JiraSoftwareHelperImpl(
                    managedIssueTypesService,
                    managedCustomFieldsService,
                    rapidViewService,
                    rapidViewQueryService,
                    sprintService
                );
            } else {
                logger.warn("JSW modules not available");
            }
        } catch (ClassNotFoundException e) {
            logger.info("Creating JSW stub helper");
            this.delegate = new JiraSoftwareHelperStub();
        }
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    @Override
    public CustomField getEpicLinkField() {
        return delegate.getEpicLinkField();
    }

    @Override
    public CustomField getRankField() {
        return delegate.getRankField();
    }

    @Override
    public CustomField getSprintField() {
        return delegate.getSprintField();
    }

    @Override
    public IssueType getEpicIssueType() {
        return delegate.getEpicIssueType();
    }

    @Override
    public RapidView findRapidViewByName(ApplicationUser user, String name) {
        return delegate.findRapidViewByName(user, name);
    }

    @Override
    public Query getRapidViewQuery(ApplicationUser user, RapidView rapidView) {
        return delegate.getRapidViewQuery(user, rapidView);
    }

    @Override
    public Collection<Sprint> findActiveSprintsByBoard(ApplicationUser user, RapidView rapidView) {
        return delegate.findActiveSprintsByBoard(user, rapidView);
    }

    @Override
    public Sprint findSprint(ApplicationUser user, RapidView rapidView, String name) {
        return delegate.findSprint(user, rapidView, name);
    }
}
