package it.ru.mail.jira.plugins.groovy;

import com.atlassian.greenhopper.api.events.board.BoardCreatedEvent;
import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.ServiceOutcome;
import com.atlassian.greenhopper.service.rapid.view.RapidViewService;
import com.atlassian.greenhopper.web.rapid.view.RapidViewPreset;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import it.ru.mail.jira.plugins.groovy.util.UserHelper;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionDescriptor;
import ru.mail.jira.plugins.groovy.api.dto.listener.ConditionType;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerDto;
import ru.mail.jira.plugins.groovy.api.dto.listener.EventListenerForm;
import ru.mail.jira.plugins.groovy.api.repository.EventListenerRepository;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class PluginClassListenerIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/pluginClassNameListenerEvent"
    );

    @Inject
    @ComponentImport
    private EventListenerRepository eventListenerRepository;
    @Inject
    @ComponentImport
    private RapidViewService rapidViewService;
    @Inject
    @ComponentImport
    private SearchRequestManager searchRequestManager;
    @Inject
    @ComponentImport
    private SearchService searchService;

    @Inject
    private UserHelper userHelper;

    private Integer listenerId;
    private RapidView rapidView;
    private SearchRequest searchRequest;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @Before
    public void beforeEach() throws Exception {
        ApplicationUser admin = userHelper.getAdmin();

        SearchService.ParseResult parseResult = searchService.parseQuery(userHelper.getUser(), "assignee = currentUser()");
        assertTrue(String.valueOf(parseResult.getErrors()), parseResult.isValid());

        searchRequest = searchRequestManager.create(new SearchRequest(parseResult.getQuery(), userHelper.getUser(), "test", null));

        String script = FileUtil.readArquillianExample("tests/pluginClassNameListenerEvent");

        EventListenerForm form = new EventListenerForm();
        form.setName("test className listener");
        form.setScriptBody(script);

        ConditionDescriptor condition = new ConditionDescriptor();
        condition.setType(ConditionType.CLASS_NAME);
        condition.setPluginKey("com.pyxis.greenhopper.jira");
        condition.setClassName(BoardCreatedEvent.class.getCanonicalName());
        form.setCondition(condition);

        EventListenerDto listener = eventListenerRepository.createEventListener(admin, form);

        this.listenerId = listener.getId();
    }

    @After
    public void afterEach() {
        ApplicationUser admin = userHelper.getAdmin();

        if (rapidView != null) {
            rapidViewService.delete(userHelper.getAdmin(), rapidView);
        }

        if (searchRequest != null) {
            searchRequestManager.delete(searchRequest.getId());
        }

        if (listenerId != null) {
            eventListenerRepository.deleteEventListener(admin, listenerId);
        }
    }

    @Test
    public void basicListenerShouldWork() throws Exception {
        ApplicationUser user = userHelper.getUser();

        rapidView = userHelper.runAsUser(
            user,
            () -> {
                ServiceOutcome<RapidView> outcome = rapidViewService
                    .create(userHelper.getUser(), "some name", searchRequest.getId(), RapidViewPreset.KANBAN);

                assertTrue(String.valueOf(outcome.getErrors()), outcome.isValid());

                return outcome.get();
            }
        );

        assertEquals("kek", rapidViewService.getRapidView(userHelper.getUser(), rapidView.getId()).getValue().getName());
    }
}
