import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.WithParam
import ru.mail.jira.plugins.jsincluder.ScriptManager
import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.WithPlugin
import ru.mail.jira.plugins.groovy.api.script.ScriptParam

@WithParam(displayName = "aaa", type = ParamType.SCRIPT, optional = false)
ScriptParam script

assert Objects.equals(script.runScript(new HashMap<String, Object>()), "test ok")

@WithPlugin("ru.mail.jira.plugins.jsincluder")
@PluginModule
ScriptManager scriptManager

return scriptManager.getScripts() != null
