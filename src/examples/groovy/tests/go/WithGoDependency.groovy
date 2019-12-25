package ru.mail.jira.scripts.go

import ru.mail.jira.plugins.groovy.api.script.GlobalObjectModule
import ru.mail.jira.scripts.go.$INJECTED_GO_CLASSNAME$

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser

public class GlobalObject$TS$ {
    private final $INJECTED_GO_CLASSNAME$ injectedGo;

    public GlobalObject$TS$(@GlobalObjectModule $INJECTED_GO_CLASSNAME$ injectedGo) {
        this.injectedGo = injectedGo;
    }

    ApplicationUser getAdmin() {
        return injectedGo.getAdmin()
    }
}
