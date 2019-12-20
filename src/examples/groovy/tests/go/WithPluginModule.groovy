import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.WithPlugin
import ru.mail.jira.plugins.jsincluder.ScriptManager
import ru.mail.jira.plugins.jsincluder.Script

@WithPlugin("ru.mail.jira.plugins.jsincluder")
class JsIncluderGlobalObject {
    private final ScriptManager scriptManager;

    JsIncluderGlobalObject(@PluginModule ScriptManager scriptManager) {
        this.scriptManager = scriptManager
    }

    Script[] getScripts() {
        return scriptManager.getScripts()
    }
}
