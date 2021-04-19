package ru.mail.jira.groovy.testgo

import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.WithPlugin
import ru.mail.jira.plugins.jsincluder.ScriptManager
import ru.mail.jira.plugins.jsincluder.Script
import ru.mail.jira.plugins.commons.LocalUtils

@WithPlugin("ru.mail.jira.plugins.jsincluder")
class JsIncluderGlobalObject {
    private final ScriptManager scriptManager;

    JsIncluderGlobalObject(@PluginModule ScriptManager scriptManager) {
        this.scriptManager = scriptManager
    }

    Script[] getScripts() {
        return scriptManager.getScripts()
    }

    String testStatics() {
        return LocalUtils.numberToCaption(0);
    }

    static String testStaticMethod() {
        return LocalUtils.numberToCaption(0)
    }
}
