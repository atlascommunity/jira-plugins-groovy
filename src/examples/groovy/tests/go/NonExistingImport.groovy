import ru.mail.jira.plugins.groovy.api.script.WithPlugin

@WithPlugin("does.not.exist")
class WithNonExistingImport {
    void test() {}
}
