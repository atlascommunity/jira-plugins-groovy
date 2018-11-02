import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.*
import ru.mail.jira.plugins.groovy.api.script.StandardModule

/**
 * some description
*/
class GroovyDocTest {
    private final UserManager userManager;

    GroovyDocTest(@StandardModule UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Returns user for current name
     * @name user name
     */
    ApplicationUser getUserByName(String name) {
        return userManager.getUserByName(name)
    }

    void voidMethod() {}

    //these should be ignored
    private void privateMethod() {}

    static void staticMethod() {}
}
