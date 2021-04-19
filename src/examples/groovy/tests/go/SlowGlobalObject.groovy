package ru.mail.jira.scripts.go

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser

public class GlobalObject$TS$ {
    GlobalObject$TS$() {
        Thread.sleep(10000);
    }

    ApplicationUser getAdmin() {
        return ComponentAccessor.userManager.getUserByName('admin')
    }
}
