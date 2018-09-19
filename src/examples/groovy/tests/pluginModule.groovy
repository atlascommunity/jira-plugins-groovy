import com.atlassian.greenhopper.api.customfield.ManagedCustomFieldsService
import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.WithPlugin

@WithPlugin("com.pyxis.greenhopper.jira")
@PluginModule
ManagedCustomFieldsService managedCustomFieldsService

return managedCustomFieldsService != null
