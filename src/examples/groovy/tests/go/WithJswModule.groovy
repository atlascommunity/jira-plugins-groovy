import com.atlassian.greenhopper.api.customfield.ManagedCustomFieldsService
import com.atlassian.jira.bc.ServiceOutcome
import com.atlassian.jira.issue.fields.CustomField
import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.WithPlugin

@WithPlugin("com.pyxis.greenhopper.jira")
class JswGlobalObject {
    private final ManagedCustomFieldsService managedCustomFieldsService;

    JswGlobalObject(@PluginModule ManagedCustomFieldsService managedCustomFieldsService) {
        this.managedCustomFieldsService = managedCustomFieldsService
    }

    ServiceOutcome<CustomField> getRankField() {
        return managedCustomFieldsService.getRankCustomField()
    }
}
