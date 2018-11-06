package ru.mail.jira.plugins.groovy.impl.var;

import groovy.lang.GroovyClassLoader;
import groovy.text.GStringTemplateEngine;
import groovy.text.TemplateEngine;
import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;

import javax.annotation.Nonnull;

public class TemplateEngineBindingDescriptor implements BindingDescriptor<TemplateEngine> {
    private final TemplateEngine templateEngine;

    public TemplateEngineBindingDescriptor(GroovyClassLoader gcl) {
        this.templateEngine = new GStringTemplateEngine(gcl);
    }

    @Override
    public TemplateEngine getValue(String scriptId) {
        return templateEngine;
    }

    @Nonnull
    @Override
    public Class<TemplateEngine> getType() {
        return TemplateEngine.class;
    }

    @Nonnull
    @Override
    public ClassDoc getDoc() {
        return new ClassDoc(
            getType().getCanonicalName(),
            "http://docs.groovy-lang.org/2.4.13/html/api/groovy/text/TemplateEngine.html"
        );
    }

    @Override
    public void dispose() throws Exception {
    }
}
