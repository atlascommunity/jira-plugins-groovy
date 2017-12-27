package ru.mail.jira.plugins.groovy.impl;

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;

public class ScriptClosure extends Closure {
    private final Class scriptClass;

    public ScriptClosure(Class scriptClass) {
        super(null);
        this.scriptClass = scriptClass;
    }

    public Object doCall(Map<String, Object> params) {
        return InvokerHelper.createScript(scriptClass, new Binding(params)).run();
    }

    public Class getScriptClass() {
        return this.scriptClass;
    }
}
