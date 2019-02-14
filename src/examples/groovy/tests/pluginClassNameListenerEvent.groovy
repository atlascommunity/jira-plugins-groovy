import com.atlassian.greenhopper.api.events.board.BoardCreatedEvent
import com.atlassian.greenhopper.manager.rapidview.RapidViewManager
import com.atlassian.greenhopper.model.rapid.RapidView
import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.WithPlugin

@WithPlugin("com.pyxis.greenhopper.jira")
@PluginModule
RapidViewManager rapidViewManager

BoardCreatedEvent event = event

rapidViewManager.update(new RapidView.RapidViewBuilder(event.getBoard()).name("kek").build())
