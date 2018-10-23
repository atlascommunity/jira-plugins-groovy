package ru.mail.jira.plugins.groovy.impl.var;

import groovy.lang.GroovyClassLoader;
import groovy.text.GStringTemplateEngine;
import groovy.text.TemplateEngine;
import ru.mail.jira.plugins.groovy.api.script.BindingDescriptor;

public class TemplateEngineBindingDescriptor implements BindingDescriptor<TemplateEngine> {
    private final TemplateEngine templateEngine;

    public TemplateEngineBindingDescriptor(GroovyClassLoader gcl) {
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
