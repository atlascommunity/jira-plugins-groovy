package ru.mail.jira.scripts.go

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser

public class GlobalObject$TS$ {
    ApplicationUser getAdmin() {
        return ComponentAccessor.userManager.getUserByName('admin')
    }
}
