import ru.mail.jira.plugins.jsincluder.ScriptManager
import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.WithPlugin

@WithPlugin("ru.mail.jira.plugins.jsincluder")
@PluginModule
ScriptManager scriptManager

return scriptManager.getScripts() != null
