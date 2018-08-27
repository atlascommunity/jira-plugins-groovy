package ru.mail.jira.plugins.groovy.impl.var;

import groovy.lang.GroovyClassLoader;
import groovy.text.GStringTemplateEngine;
import groovy.text.TemplateEngine;

public class TemplateEngineGlobalVariable implements GlobalVariable<TemplateEngine> {
    private final TemplateEngine templateEngine;

    public TemplateEngineGlobalVariable(GroovyClassLoader gcl) {
        this.templateEngine = new GStringTemplateEngine(gcl);
    }

    @Override
    public TemplateEngine getValue(String scriptId) {
        return templateEngine;
    }

    @Override
    public Class<TemplateEngine> getType() {
        return TemplateEngine.class;
    }

    @Override
    public void dispose() throws Exception {
    }
}
